package com.chaotic_loom.loom.core.rendering.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Describes a custom shader and owns its live {@link ManagedShaderInstance}.
 *
 * <p><b>Usage</b></p>
 * <pre>{@code
 * // 1. Declare at mod-init time (no GPU resources yet)
 * ShaderProgram MY_SHADER = ShaderProgram.builder(
 *         new ResourceLocation("mymod", "my_shader"),
 *         DefaultVertexFormat.POSITION_COLOR)
 *     .trackUniforms("Brightness", "Tint")
 *     .build();
 *
 * // 2. Register during resource reload
 * ShaderRegistry.getInstance().registerAll(provider, pairList);
 *
 * // 3. Use getInstance() in platform-specific code to build a ShaderStateShard
 * }</pre>
 *
 * <p>Instances are immutable after {@link Builder#build()}; only the internal
 * {@link ManagedShaderInstance} reference is replaced on each reload.
 */
public final class ShaderProgram {

    // -------------------------------------------------------------------------
    // Identity (set once at construction)
    // -------------------------------------------------------------------------

    private final ResourceLocation location;
    private final VertexFormat vertexFormat;

    /**
     * Uniform names whose JSON-parsed defaults should be snapshotted.
     * An empty set means "snapshot all uniforms".
     */
    private final Set<String> trackedUniforms;

    // -------------------------------------------------------------------------
    // Mutable live state (replaced each resource reload)
    // -------------------------------------------------------------------------

    private volatile ManagedShaderInstance instance;

    // -------------------------------------------------------------------------
    // Construction via Builder
    // -------------------------------------------------------------------------

    private ShaderProgram(Builder builder) {
        this.location = builder.location;
        this.vertexFormat = builder.vertexFormat;
        this.trackedUniforms = Collections.unmodifiableSet(new LinkedHashSet<>(builder.trackedUniforms));
    }

    // -------------------------------------------------------------------------
    // Public accessors
    // -------------------------------------------------------------------------

    public ResourceLocation getLocation() {
        return location;
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    /**
     * Returns the set of uniform names that will have their defaults snapshotted.
     * An empty set means every uniform in the shader JSON is tracked.
     */
    public Set<String> getTrackedUniforms() {
        return trackedUniforms;
    }

    /**
     * Returns the live {@link ManagedShaderInstance}, or {@code null} if no
     * resource load has occurred yet.
     */
    public ManagedShaderInstance getInstance() {
        return instance;
    }

    // -------------------------------------------------------------------------
    // Lifecycle — called by ShaderRegistry / resource reload hooks
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@link ManagedShaderInstance} from the given provider.
     * Called by {@link ShaderRegistry} during resource loading.
     */
    ManagedShaderInstance buildInstance(ResourceProvider provider) throws IOException {
        ManagedShaderInstance built = new ManagedShaderInstance(provider, location, vertexFormat, this);
        this.instance = built;
        return built;
    }

    /**
     * Replaces the live instance after a resource reload.
     * The cast is safe because we only ever produce {@link ManagedShaderInstance} objects.
     */
    void onReload(ShaderInstance reloaded) {
        this.instance = (ManagedShaderInstance) reloaded;
    }

    // -------------------------------------------------------------------------
    // Convenience registration helper
    // -------------------------------------------------------------------------

    /**
     * Registers one or more {@link ShaderProgram}s into the pair list expected
     * by Fabric's {@code RegisterShadersCallback} (or equivalent).
     *
     * @param provider resource provider from the reload event
     * @param pairList mutable list that collects (instance, reloadConsumer) pairs
     * @param programs programs to register
     */
    public static void registerAll(ResourceProvider provider,
                                   List<Pair<ShaderInstance, Consumer<ShaderInstance>>> pairList,
                                   ShaderProgram... programs) throws IOException {
        for (ShaderProgram program : programs) {
            pairList.add(Pair.of(
                    program.buildInstance(provider),
                    program::onReload
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder(ResourceLocation location, VertexFormat vertexFormat) {
        return new Builder(location, vertexFormat);
    }

    public static final class Builder {

        private final ResourceLocation location;
        private final VertexFormat vertexFormat;
        private final Set<String> trackedUniforms = new LinkedHashSet<>();

        private Builder(ResourceLocation location, VertexFormat vertexFormat) {
            this.location = Objects.requireNonNull(location, "location");
            this.vertexFormat = Objects.requireNonNull(vertexFormat, "vertexFormat");
        }

        /**
         * Names the uniforms whose JSON defaults should be snapshotted.
         * If this method is never called (or called with no arguments), every
         * uniform in the shader is tracked.
         */
        public Builder trackUniforms(String... names) {
            trackedUniforms.addAll(Arrays.asList(names));
            return this;
        }

        public ShaderProgram build() {
            return new ShaderProgram(this);
        }
    }
}