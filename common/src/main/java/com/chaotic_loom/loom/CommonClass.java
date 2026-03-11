package com.chaotic_loom.loom;

import com.chaotic_loom.loom.builtin.packets.LoomBuiltInPackets;
import com.chaotic_loom.loom.builtin.shaders.BuiltinTestShader;
import com.chaotic_loom.loom.platform.Services;

public class CommonClass {
    public static void init() {
        Constants.LOG.info("────────────────────────────────────────────────────────────");
        Constants.LOG.info("Starting loom...");
        Constants.LOG.info("────────────────────────────────────────────────────────────");
        Constants.LOG.info(" - Loader: {}", Services.PLATFORM.getPlatformName());
        Constants.LOG.info(" - Environment: {}", Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info(" - Mod version: {}", Services.PLATFORM.getModVersion());
        Constants.LOG.info(" - Is dev env: {}", Services.PLATFORM.isDevelopmentEnvironment());
        Constants.LOG.info("────────────────────────────────────────────────────────────");

        LoomBuiltInPackets.init();

        if (Services.PLATFORM.isClient()) {
            Constants.LOG.info("Starting client side logic...");
            BuiltinTestShader.init();
        }
    }
}