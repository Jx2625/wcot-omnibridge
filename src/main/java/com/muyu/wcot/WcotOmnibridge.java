package com.muyu.wcot;

import com.muyu.wcot.network.WcwtSelectOmniversalRecipePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(WcotOmnibridge.MOD_ID)
public class WcotOmnibridge {

    public static final String MOD_ID = "wcot_omnibridge";
    public static final Logger LOGGER = LoggerFactory.getLogger("WCOT-Omnibridge");

    public WcotOmnibridge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("WCWT Omniversal Bridge loaded.");
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(
                WcwtSelectOmniversalRecipePacket.TYPE,
                WcwtSelectOmniversalRecipePacket.STREAM_CODEC,
                WcwtSelectOmniversalRecipePacket::handle);
    }
}