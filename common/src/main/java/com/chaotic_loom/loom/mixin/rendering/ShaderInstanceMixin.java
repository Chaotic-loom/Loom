package com.chaotic_loom.loom.mixin.rendering;

import com.chaotic_loom.loom.core.rendering.shader.ManagedShaderInstance;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.gson.JsonElement;

/**
 * Patches {@code ShaderInstance} constructor to fix the shader-JSON
 * {@link net.minecraft.resources.ResourceLocation} for mod-namespaced shaders.
 *
 * <p><b>The problem</b></p>
 * Inside {@code ShaderInstance.<init>}, Mojang builds the path to the shader
 * JSON like this (simplified):
 * <pre>
 *   new ResourceLocation("shaders/core/" + name + ".json")
 * </pre>
 * When {@code name} contains a namespace (e.g. {@code "mymod:my_shader"}), the
 * resulting string becomes {@code "shaders/core/mymod:my_shader.json"}, which is
 * an <em>invalid</em> ResourceLocation because colons are not allowed in the path
 * segment.
 *
 * <p><b>The fix</b></p>
 * We intercept the {@code new ResourceLocation(String)} call and restructure the
 * string into the correct form: {@code "mymod:shaders/core/my_shader.json"}.
 *
 * <p>The injection is scoped to {@link ManagedShaderInstance} only, so vanilla
 * {@link ShaderInstance} construction is completely unaffected.
 */
@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {

    @Shadow @Final private String name;

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;<init>(Ljava/lang/String;)V"
            ),
            allow = 1
    )
    private String fixModdedShaderProgramId(String rawId) {
        if (!((Object) this instanceof ManagedShaderInstance)) {
            return rawId; // leave vanilla shaders completely untouched
        }
        return loom$rewriteToNamespacedPath(rawId);
    }

    /**
     * Converts a malformed path like {@code "shaders/core/mymod:my_shader.json"}
     * into the valid namespaced form {@code "mymod:shaders/core/my_shader.json"}.
     *
     * <p>If the raw ID contains no colon (a vanilla path), it is returned as-is.
     */
    @Unique
    private static String loom$rewriteToNamespacedPath(String rawId) {
        int colonIndex = rawId.indexOf(':');
        if (colonIndex < 0) {
            return rawId; // no namespace embedded, nothing to rewrite
        }

        // rawId = "shaders/core/mymod:my_shader.json"
        // before = "shaders/core/mymod"
        // after = "my_shader.json"
        String before = rawId.substring(0, colonIndex);
        String after = rawId.substring(colonIndex + 1);

        int lastSlash = before.lastIndexOf('/');
        String namespace = before.substring(lastSlash + 1); // "mymod"
        String pathDir = before.substring(0, lastSlash + 1); // "shaders/core/"

        // result = "mymod:shaders/core/my_shader.json"
        return namespace + ":" + pathDir + after;
    }

    /**
     * Injects at the tail of {@code ShaderInstance.parseUniformNode} (which is
     * {@code final} and cannot be overridden) to let {@link ManagedShaderInstance}
     * snapshot the uniform value that was just parsed.
     */
    @Inject(
            method = "parseUniformNode",
            at = @At("TAIL")
    )
    private void onParseUniformNode(JsonElement jsonElement, CallbackInfo ci) {
        if ((Object) this instanceof ManagedShaderInstance managed) {
            managed.captureUniform(jsonElement);
        }
    }
}