package com.chaotic_loom.loom;

import net.fabricmc.api.ModInitializer;

public class FabricEntry implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonClass.init();
    }
}
