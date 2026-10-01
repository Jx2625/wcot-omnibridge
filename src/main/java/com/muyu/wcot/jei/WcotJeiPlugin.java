package com.muyu.wcot.jei;

import com.muyu.wcot.WcotOmnibridge;
import com.sorrowmist.useless.compat.jei.AdvancedAlloyFurnaceRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class WcotJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(
                WcotOmnibridge.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new WcwtOmniversalTransferHandler(registration.getTransferHelper()),
                AdvancedAlloyFurnaceRecipeCategory.TYPE);
    }
}