package com.chaotic_loom.loom;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ExampleMod {
    public ExampleMod(IEventBus eventBus) {
        CommonClass.init();
    }
}