package com.chaotic_loom.loom;

import com.chaotic_loom.loom.builtin.shaders.BuiltinTestShader;
import com.chaotic_loom.loom.platform.Services;

public class CommonClass {
    public static void init() {
        Constants.LOG.info("Starting library...");

        if (Services.PLATFORM.isClient()) {
            BuiltinTestShader.init();
        }
    }
}