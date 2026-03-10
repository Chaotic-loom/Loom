package com.chaotic_loom.loom.core.rendering.shader;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central registry for all {@link ShaderProgram}s in the mod.
 *
 * <p><b>Lifecycle</b></p>
 * <ol>
 *   <li><b>Mod init</b> — call {@link #register} to declare programs (no GPU
 *       resources allocated yet). The registry auto-subscribes to
 *       {@link ShaderRegistrationCallback}, so no manual reload hook is needed
 *       in common or mod code.</li>
 *   <li><b>Resource reload</b> — handled automatically. The platform bridge fires
 *       {@link ShaderRegistrationCallback#EVENT}; this registry responds by
 *       building all registered programs into the pair list.</li>
 *   <li><b>Render time</b> — use {@link #get} or hold a direct reference to a
 *       {@link ShaderProgram}; {@link ShaderProgram#getInstance()} always reflects
 *       the current post-reload instance.</li>
 * </ol>
 *
 * <p>Thread-safety: {@link #add} and {@link #get} use a {@link ConcurrentHashMap}
 * so they are safe to call from any thread. {@link #registerAll} is invoked by
 * {@link ShaderRegistrationCallback} on the render thread, as required by
 * Minecraft's shader system.
 */
public final class ShaderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShaderRegistry.class);

    /** Singleton instance. */
    private static final ShaderRegistry INSTANCE = new ShaderRegistry();

    private final Map<ResourceLocation, ShaderProgram> registry = new ConcurrentHashMap<>();

    private ShaderRegistry() {
        // Auto-subscribe so callers never need to wire up the reload hook manually.
        // The platform bridge fires ShaderRegistrationCallback.EVENT; we respond
        // by building every registered ShaderProgram into the pair list.
        ShaderRegistrationCallback.EVENT.register(this::registerAll);
    }

    public static ShaderRegistry getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Program registration
    // -------------------------------------------------------------------------

    /**
     * Declares a {@link ShaderProgram} so it is included in the next
     * resource-reload cycle. Safe to call during mod initialisation from any thread.
     *
     * @return the same {@code program} for chaining
     */
    public ShaderProgram add(ShaderProgram program) {
        ShaderProgram previous = registry.put(program.getLocation(), program);
        if (previous != null && previous != program) {
            LOGGER.warn("[ShaderRegistry] Replacing existing program '{}' — was this intentional?",
                    program.getLocation());
        }
        return program;
    }

    /**
     * Looks up a program by its {@link ResourceLocation}.
     *
     * @return the registered program, or {@link Optional#empty()} if not found
     */
    public Optional<ShaderProgram> get(ResourceLocation location) {
        return Optional.ofNullable(registry.get(location));
    }

    /** Returns an unmodifiable snapshot of all registered programs. */
    public Collection<ShaderProgram> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    // -------------------------------------------------------------------------
    // Resource-reload — called automatically via ShaderRegistrationCallback
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link ManagedShaderInstance} for every registered program and
     * appends the resulting pairs to {@code pairList}.
     *
     * <p>This method is invoked automatically by the {@link ShaderRegistrationCallback}
     * subscription established in the constructor. You do not need to call it
     * directly unless you are building a custom reload pipeline.
     *
     * <p>Failures for individual programs are isolated: a missing or malformed
     * shader JSON will be logged but will not abort the remaining registrations.
     *
     * @param provider  resource provider supplied by the reload event
     * @param pairList  mutable list that Minecraft's shader loader reads from
     */
    public void registerAll(ResourceProvider provider,
                            List<Pair<ShaderInstance, Consumer<ShaderInstance>>> pairList) {
        for (ShaderProgram program : registry.values()) {
            try {
                pairList.add(Pair.of(
                        program.buildInstance(provider),
                        program::onReload
                ));
            } catch (IOException e) {
                LOGGER.error("[ShaderRegistry] Failed to build shader '{}': {}",
                        program.getLocation(), e.getMessage(), e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Shorthand for {@link #add}: builds and registers a program in one call.
     *
     * <pre>{@code
     * public static final ShaderProgram MY_SHADER = ShaderRegistry.getInstance()
     *     .register(ShaderProgram.builder(loc, fmt).trackUniforms("Tint").build());
     * }</pre>
     */
    public ShaderProgram register(ShaderProgram program) {
        return add(program);
    }
}