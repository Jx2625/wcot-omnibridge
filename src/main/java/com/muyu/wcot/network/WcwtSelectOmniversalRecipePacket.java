package com.muyu.wcot.network;

import com.lhy.wcwt.menu.WirelessComprehensiveWorkTerminalMenu;
import com.sorrowmist.useless.content.machines.advanced_alloy_furnace.ae.PendingOmniversalPatternHolder;
import com.sorrowmist.useless.content.recipe.AlloyFurnaceRecipeIdentity;
import com.sorrowmist.useless.content.recipe.RecipeSourceIds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 告诉服务端：玩家刚从 JEI 把哪条合金炉配方传进了 WCWT 编码终端。
 *
 * 不复用 UselessMod 的 SelectOmniversalPatternRecipePacket，因为那个包的 handler
 * 只认 AE2 原版的 PatternEncodingTermMenu，不认 WCWT 的菜单类。这个包直接对
 * WCWT 菜单的 PatternEncodingLogic 操作，绕过 instanceof 限制。
 */
public record WcwtSelectOmniversalRecipePacket(
        int containerId, ResourceLocation recipeId, String fingerprint, String sourceId)
        implements CustomPacketPayload {

    public WcwtSelectOmniversalRecipePacket {
        sourceId = RecipeSourceIds.normalize(sourceId);
    }

    public static final Type<WcwtSelectOmniversalRecipePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("wcot_omnibridge",
                    "select_omniversal_recipe"));

    public static final StreamCodec<FriendlyByteBuf, WcwtSelectOmniversalRecipePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeVarInt(packet.containerId);
                        ResourceLocation.STREAM_CODEC.encode(buffer, packet.recipeId);
                        buffer.writeUtf(packet.fingerprint);
                        buffer.writeUtf(packet.sourceId);
                    },
                    buffer -> new WcwtSelectOmniversalRecipePacket(
                            buffer.readVarInt(),
                            ResourceLocation.STREAM_CODEC.decode(buffer),
                            buffer.readUtf(), buffer.readUtf()));

    public static void handle(WcwtSelectOmniversalRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof WirelessComprehensiveWorkTerminalMenu menu)) return;
            if (menu.containerId != packet.containerId) return;

            var menuHost = menu.getMenuHost();
            if (menuHost == null) return;
            var logic = menuHost.getLogic();
            if (!(logic instanceof PendingOmniversalPatternHolder holder)) return;

            AlloyFurnaceRecipeIdentity identity;
            try {
                identity = new AlloyFurnaceRecipeIdentity(packet.recipeId, packet.fingerprint);
            } catch (RuntimeException ignored) {
                holder.uselessMod$setPendingOmniversalRecipe(null);
                holder.uselessMod$setPendingOmniversalSourceId(null);
                return;
            }
            holder.uselessMod$setPendingOmniversalRecipe(identity);
            holder.uselessMod$setPendingOmniversalSourceId(packet.sourceId());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}