package com.chaotic_loom.loom;

import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ForgeEntry {
    public ForgeEntry() {
        CommonClass.init();
    }
}