package com.chaotic_loom.loom.core.rendering.shader;

import com.chaotic_loom.loom.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@link ShaderInstance} subclass that:
 * <ul>
 *   <li>Holds a back-reference to the owning {@link ShaderProgram}.</li>
 *   <li>Captures default uniform values from the shader JSON during parse.</li>
 *   <li>Exposes {@link #restoreUniformDefaults()} so render code can reset
 *       per-frame uniforms without reparsing the JSON.</li>
 * </ul>
 *
 * <p>Instances are created exclusively by {@link ShaderProgram#buildInstance}.
 * Do not instantiate directly.
 */
public final class ManagedShaderInstance extends ShaderInstance {

    private final ShaderProgram owner;

    /**
     * Uniform name → snapshot captured at parse time.
     * Populated by {@link #captureUniform} (called via mixin).
     */
    private final Map<String, UniformSnapshot> capturedDefaults = new HashMap<>();

    ManagedShaderInstance(ResourceProvider provider,
                          ResourceLocation location,
                          VertexFormat format,
                          ShaderProgram owner) throws IOException {
        super(provider, location.toString(), format);
        System.out.println("HERE_HERE_HERE_HERE_HERE_HERE_HERE_HERE_HERE");
        System.out.println(owner);
        this.owner = owner;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the {@link ShaderProgram} that owns this instance. */
    public ShaderProgram getOwner() {
        return owner;
    }

    /**
     * Resets every cached uniform to the value it had when the shader JSON
     * was first parsed.  Call this at the start of each render pass to undo
     * any per-object uniform mutations from the previous frame.
     */
    public void restoreUniformDefaults() {
        for (Map.Entry<String, UniformSnapshot> entry : capturedDefaults.entrySet()) {
            Uniform uniform = super.uniformMap.get(entry.getKey());
            if (uniform != null) {
                entry.getValue().applyTo(uniform);
            }
        }
    }

    /**
     * Returns a read-only view of all captured uniform defaults.
     * Useful for introspection and tooling.
     */
    public Map<String, UniformSnapshot> getCapturedDefaults() {
        return Collections.unmodifiableMap(capturedDefaults);
    }

    // -------------------------------------------------------------------------
    // Called by ShaderInstanceMixin — capture defaults during JSON parsing
    // -------------------------------------------------------------------------

    /**
     * Invoked by {@link com.chaotic_loom.loom.mixin.rendering.ShaderInstanceMixin}
     * at the tail of {@code ShaderInstance.parseUniformNode}, which is {@code final}
     * and cannot be overridden directly.
     *
     * <p>Reads the uniform name from {@code jsonElement} and, if it belongs to
     * the tracked set (or all uniforms are tracked), snapshots the parsed value
     * into {@link #capturedDefaults}.
     */
    public void captureUniform(JsonElement jsonElement) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "uniform");
        String uniformName = GsonHelper.getAsString(jsonObject, "name");

        Set<String> tracked = owner.getTrackedUniforms();
        if (!tracked.isEmpty() && !tracked.contains(uniformName)) {
            return; // not in the explicit tracking list — skip
        }

        // parseUniformNode always appends to `uniforms`, so the last element
        // is the one that was just parsed.
        if (!super.uniforms.isEmpty()) {
            Uniform uniform = super.uniforms.get(super.uniforms.size() - 1);
            capturedDefaults.put(uniformName, UniformSnapshot.capture(uniform));
        }
    }
}