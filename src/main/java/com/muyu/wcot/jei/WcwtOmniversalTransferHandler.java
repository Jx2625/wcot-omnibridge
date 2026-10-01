package com.muyu.wcot.jei;

import appeng.api.stacks.GenericStack;
import appeng.parts.encoding.EncodingMode;
import com.lhy.wcwt.init.ModMenus;
import com.lhy.wcwt.menu.WirelessComprehensiveWorkTerminalMenu;
import com.lhy.wcwt.network.JeiCraftingTransferPacket;
import com.muyu.wcot.network.WcwtSelectOmniversalRecipePacket;
import com.sorrowmist.useless.compat.jei.AdvancedAlloyFurnaceRecipeCategory;
import com.sorrowmist.useless.compat.jei.OmniversalPatternJeiTransferHandler;
import com.sorrowmist.useless.content.recipe.AdvancedAlloyFurnaceRecipe;
import com.sorrowmist.useless.content.recipe.AlloyFurnaceRecipeCatalog;
import com.sorrowmist.useless.content.recipe.AlloyFurnaceRecipeFingerprint;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 让 WCWT 的无线综合工作终端也能通过 JEI 的 “+” 按钮传输合金炉配方，
 * 并触发万象样板的转换。
 *
 * JEI 会优先选择“配方类型 + 菜单类型”都匹配的 handler，所以注册这个之后，
 * 合金炉配方在 WCWT 里就由本 handler 处理，WCWT 自带的 universal handler 不会再插手。
 */
public class WcwtOmniversalTransferHandler
        implements IRecipeTransferHandler<WirelessComprehensiveWorkTerminalMenu,
        AlloyFurnaceRecipeCatalog.Entry> {

    private final IRecipeTransferHandlerHelper helper;

    public WcwtOmniversalTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends WirelessComprehensiveWorkTerminalMenu> getContainerClass() {
        return WirelessComprehensiveWorkTerminalMenu.class;
    }

    @Override
    public Optional<MenuType<WirelessComprehensiveWorkTerminalMenu>> getMenuType() {
        return Optional.of(ModMenus.WCWT_MENU_TYPE);
    }

    @Override
    public RecipeType<AlloyFurnaceRecipeCatalog.Entry> getRecipeType() {
        return AdvancedAlloyFurnaceRecipeCategory.TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            WirelessComprehensiveWorkTerminalMenu menu,
            AlloyFurnaceRecipeCatalog.Entry entry,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {

        AdvancedAlloyFurnaceRecipe recipe = entry.recipe();

        // 复用 UselessMod 的公开 API 收集输入
        List<List<GenericStack>> inputOptions =
                OmniversalPatternJeiTransferHandler.inputOptions(recipe);
        List<GenericStack> outputs = buildOutputs(recipe);

        if (inputOptions == null || inputOptions.isEmpty() || outputs.isEmpty()) {
            return helper.createInternalError();
        }

        // 槽位数量检查
        int maxIn = appeng.crafting.pattern.AEProcessingPattern.MAX_INPUT_SLOTS;
        int maxOut = appeng.crafting.pattern.AEProcessingPattern.MAX_OUTPUT_SLOTS;
        if (inputOptions.size() > maxIn || outputs.size() > maxOut) {
            return helper.createUserErrorWithTooltip(
                    Component.translatable("gui.useless_mod.omniversal_pattern.too_many_slots"));
        }

        if (!doTransfer) {
            return null;
        }

        // 1. 发送配方身份（我们自己的包，服务端 handler 认 WCWT 菜单）
        PacketDistributor.sendToServer(new WcwtSelectOmniversalRecipePacket(
                menu.containerId,
                recipe.id(),
                AlloyFurnaceRecipeFingerprint.create(recipe, player.level().registryAccess()),
                entry.sourceId()));

        // 2. 填充编码槽位
        // WCWT 的 JeiCraftingTransferPacket 接受 List<GenericStack>，每个槽一个候选。
        // 这里把多候选项拍平，取每个输入的第一个候选作为代表。
        List<GenericStack> flatInputs = new ArrayList<>(inputOptions.size());
        for (List<GenericStack> options : inputOptions) {
            flatInputs.add(options.isEmpty() ? null : options.get(0));
        }

        PacketDistributor.sendToServer(new JeiCraftingTransferPacket(
                flatInputs, outputs, false, EncodingMode.PROCESSING));

        return null;
    }

    /** 和 UselessMod 内部的 outputs() 逻辑一致，但那个方法是 private，所以这里自己拼。 */
    private static List<GenericStack> buildOutputs(AdvancedAlloyFurnaceRecipe recipe) {
        List<GenericStack> result = new ArrayList<>();
        for (var stack : recipe.outputs()) {
            GenericStack converted = GenericStack.fromItemStack(stack);
            if (converted != null) result.add(converted);
        }
        for (var fluid : recipe.outputFluids()) {
            GenericStack converted = GenericStack.fromFluidStack(fluid);
            if (converted != null) result.add(converted);
        }
        result.addAll(recipe.keyOutputs());
        return result;
    }
}