package com.muyu.wcot.mixin;

import appeng.parts.encoding.EncodingMode;
import appeng.parts.encoding.PatternEncodingLogic;
import com.lhy.wcwt.menu.WirelessComprehensiveWorkTerminalMenu;
import com.sorrowmist.useless.content.machines.advanced_alloy_furnace.ae.PendingOmniversalPatternHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(value = WirelessComprehensiveWorkTerminalMenu.class, remap = false)
public abstract class WcwtEncodePatternMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("WCOT-Omnibridge");

    @Shadow(remap = false)
    private PatternEncodingLogic patternEncodingLogic;

    /**
     * WCWT 的 encodePattern 把样板写进槽位后，主动调用 UselessMod 的转换入口。
     * 因为 WCWT 用 Slot.set() 写槽位，不会触发 PatternEncodingLogic.onChangeInventory，
     * 所以 UselessMod 原版的 Mixin 不会自动跑，必须在这里手动触发。
     */
    @Inject(
            method = "encodePattern(Lappeng/parts/encoding/EncodingMode;ZLjava/lang/String;Z)V",
            at = @At("TAIL"),
            remap = false
    )
    private void wcot$tryConvertAfterEncode(EncodingMode mode, boolean uploadEnabled,
                                            String providerSearchText, boolean fallbackToEditSlot,
                                            CallbackInfo ci) {
        if (mode != EncodingMode.PROCESSING) return;
        if (patternEncodingLogic == null) return;

        if (patternEncodingLogic instanceof PendingOmniversalPatternHolder holder) {
            try {
                holder.uselessMod$tryConvertPendingOmniversalPattern();
            } catch (RuntimeException e) {
                LOGGER.warn("WCOT: conversion after encode threw", e);
            }
        } else {
            LOGGER.debug("WCOT: PatternEncodingLogic is not a PendingOmniversalPatternHolder; " +
                    "UselessMod mixin may not be applied.");
        }
    }
}