package com.chaotic_loom.loom.core.rendering.shader;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A loader-agnostic event fired whenever Minecraft is about to process its
 * shader pair list during a resource reload.
 *
 * <p><b>How it fits into the system</b></p>
 * <pre>
 *   VANILLA (both loaders)
 *   ──────────────────────
 *   GameRenderer.reloadShaders(ResourceProvider)
 *        │  builds vanilla shader pair list
 *        │
 *        ▼
 *   GameRendererMixin  ← @Inject before list.forEach(...)
 *        │  calls ShaderRegistrationCallback.EVENT.invoke(provider, list)
 *        │
 *        ▼
 *   ShaderRegistrationCallback.EVENT
 *        │  notifies all registered handlers
 *        │
 *        ├──▶ ShaderRegistry  (auto-subscribed in its constructor)
 *        │         builds a ManagedShaderInstance for every ShaderProgram
 *        │
 *        └──▶ any other handler registered via EVENT.register(...)
 *
 *   GameRenderer continues — iterates the now-extended list,
 *   compiles every shader (vanilla + ours), calls each Consumer.
 * </pre>
 *
 * <p>No platform API (Fabric, Forge, NeoForge) is involved anywhere in this
 * chain. The mixin targets vanilla {@code GameRenderer} directly.
 *
 * <p><b>Registering a handler</b></p>
 * <pre>{@code
 * ShaderRegistrationCallback.EVENT.register((provider, pairList) -> {
 *     // append Pair.of(instance, reloadConsumer) entries to pairList
 * });
 * }</pre>
 *
 * <p>Handlers are invoked in registration order. A handler that throws will have
 * its exception logged; remaining handlers still run.
 */
public final class ShaderRegistrationCallback {

    /** The singleton event instance. */
    public static final ShaderRegistrationCallback EVENT = new ShaderRegistrationCallback();

    /**
     * Functional interface for shader registration handlers.
     *
     * <p>{@code pairList} is the live list that {@code GameRenderer} will iterate
     * after this event returns — append {@code Pair.of(instance, reloadConsumer)}
     * entries to it and Minecraft will compile and finalise them automatically.
     */
    @FunctionalInterface
    public interface Handler {
        void onRegisterShaders(
                ResourceProvider provider,
                List<Pair<ShaderInstance, Consumer<ShaderInstance>>> pairList);
    }

    /**
     * Thread-safe so that handlers can be registered from any thread during
     * mod initialization without external synchronization.
     */
    private final List<Handler> handlers = new CopyOnWriteArrayList<>();

    private ShaderRegistrationCallback() {}

    // -------------------------------------------------------------------------
    // Handler registration
    // -------------------------------------------------------------------------

    /**
     * Registers a handler to be called each time shaders are loaded or reloaded.
     * Safe to call from any thread at any point before the first resource reload.
     *
     * @param handler the handler to register; must not be {@code null}
     */
    public void register(Handler handler) {
        if (handler == null) throw new NullPointerException("handler must not be null");
        handlers.add(handler);
    }

    // -------------------------------------------------------------------------
    // Invocation — called by GameRendererMixin only
    // -------------------------------------------------------------------------

    /**
     * Fires the event, invoking every registered handler in order.
     *
     * <p>Called exclusively by
     * {@link com.chaotic_loom.loom.mixin.rendering.GameRendererMixin}
     * from within {@code GameRenderer.reloadShaders}. Do not call this from mod
     * code or from any platform-specific class.
     *
     * <p>Handler failures are isolated: an exception from one handler is logged
     * and the remaining handlers still run.
     *
     * @param provider  resource provider from the reload
     * @param pairList  the live shader pair list that {@code GameRenderer} will process
     */
    public void invoke(ResourceProvider provider,
                       List<Pair<ShaderInstance, Consumer<ShaderInstance>>> pairList) {
        for (Handler handler : handlers) {
            try {
                handler.onRegisterShaders(provider, pairList);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(ShaderRegistrationCallback.class)
                        .error("[ShaderRegistrationCallback] Handler '{}' threw an exception: {}",
                                handler, e.getMessage(), e);
            }
        }
    }
}