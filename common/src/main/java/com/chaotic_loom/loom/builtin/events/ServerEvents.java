package com.chaotic_loom.loom.builtin.events;

import com.chaotic_loom.loom.core.events.Event;
import com.chaotic_loom.loom.core.events.EventFactory;
import com.chaotic_loom.loom.core.events.EventResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class ServerEvents {
    @FunctionalInterface
    public interface WorldLoad {
        void onEvent(MinecraftServer server);
    }
    public static final Event<WorldLoad> WORLD_LOAD = EventFactory.createVoid(WorldLoad.class, callbacks -> (server) -> {
        for (WorldLoad callback : callbacks) {
            callback.onEvent(server);
        }
    });



    @FunctionalInterface
    public interface WorldUnload {
        void onEvent(MinecraftServer server);
    }
    public static final Event<WorldUnload> WORLD_UNLOAD = EventFactory.createVoid(WorldUnload.class, callbacks -> (server) -> {
        for (WorldUnload callback : callbacks) {
            callback.onEvent(server);
        }
    });



    @FunctionalInterface
    public interface TickStart {
        void onEvent(MinecraftServer server);
    }
    public static final Event<TickStart> TICK_START = EventFactory.createVoid(TickStart.class, callbacks -> (server) -> {
        for (TickStart callback : callbacks) {
            callback.onEvent(server);
        }
    });



    @FunctionalInterface
    public interface TickEnd {
        void onEvent(MinecraftServer server);
    }
    public static final Event<TickEnd> TICK_END = EventFactory.createVoid(TickEnd.class, callbacks -> (server) -> {
        for (TickEnd callback : callbacks) {
            callback.onEvent(server);
        }
    });


    @FunctionalInterface
    public interface ChunkLoad {
        void onEvent(ServerLevel serverLevel, LevelChunk levelChunk);
    }
    public static final Event<ChunkLoad> CHUNK_LOAD = EventFactory.createVoid(ChunkLoad.class, callbacks -> (serverLevel, levelChunk) -> {
        for (ChunkLoad callback : callbacks) {
            callback.onEvent(serverLevel, levelChunk);
        }
    });



    @FunctionalInterface
    public interface ChunkUnload {
        void onEvent(ServerLevel serverLevel, LevelChunk levelChunk);
    }
    public static final Event<ChunkUnload> CHUNK_UNLOAD = EventFactory.createVoid(ChunkUnload.class, callbacks -> (serverLevel, levelChunk) -> {
        for (ChunkUnload callback : callbacks) {
            callback.onEvent(serverLevel, levelChunk);
        }
    });



    @FunctionalInterface
    public interface PlayerJoin {
        EventResult onEvent(MinecraftServer server, ServerPlayer player);
    }
    public static final Event<PlayerJoin> PLAYER_JOIN = EventFactory.createCancellable(PlayerJoin.class, callbacks -> (server, player) -> {
        for (PlayerJoin callback : callbacks) {
            EventResult result = callback.onEvent(server, player);
            if (result == EventResult.CANCEL) {
                return EventResult.CANCEL;
            }
        }
        return EventResult.PASS;
    });



    @FunctionalInterface
    public interface EntityLoad {
        EventResult onEvent(ServerLevel serverLevel, Entity entity);
    }
    public static final Event<EntityLoad> ENTITY_LOAD = EventFactory.createCancellable(EntityLoad.class, callbacks -> (serverLevel, entity) -> {
        for (EntityLoad callback : callbacks) {
            EventResult result = callback.onEvent(serverLevel, entity);
            if (result == EventResult.CANCEL) {
                return EventResult.CANCEL;
            }
        }
        return EventResult.PASS;
    });



    @FunctionalInterface
    public interface EntityUnload {
        EventResult onEvent(ServerLevel serverLevel, Entity entity);
    }
    public static final Event<EntityUnload> ENTITY_UNLOAD = EventFactory.createCancellable(EntityUnload.class, callbacks -> (serverLevel, entity) -> {
        for (EntityUnload callback : callbacks) {
            EventResult result = callback.onEvent(serverLevel, entity);
            if (result == EventResult.CANCEL) {
                return EventResult.CANCEL;
            }
        }
        return EventResult.PASS;
    });



    @FunctionalInterface
    public interface EntityDie {
        EventResult onEvent(LivingEntity livingEntity, DamageSource damageSource);
    }
    public static final Event<EntityDie> ENTITY_DIE = EventFactory.createCancellable(EntityDie.class, callbacks -> (livingEntity, damageSource) -> {
        for (EntityDie callback : callbacks) {
            EventResult result = callback.onEvent(livingEntity, damageSource);
            if (result == EventResult.CANCEL) {
                return EventResult.CANCEL;
            }
        }
        return EventResult.PASS;
    });
}
