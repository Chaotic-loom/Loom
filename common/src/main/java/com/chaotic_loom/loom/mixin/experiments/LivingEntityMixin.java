package com.chaotic_loom.loom.mixin.experiments;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    // Shadow the method so we can check for the glowing effect
    @Shadow
    public boolean hasEffect(MobEffect effect) {
        throw new RuntimeException("Impossible exception");
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void loom$debugGlowingId(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // isGlowing() checks the SynchedEntityData flag, which is perfectly
        // synced to the client by the server whenever the potion effect is applied!
        if (entity.tickCount % 20 == 0 && entity.isCurrentlyGlowing()) {
            String side = entity.level().isClientSide ? "CLIENT" : "SERVER";
            System.out.println(">>> [" + side + "] Glowing Entity ID: " + entity.getId() + " | Type: " + entity.getType().toShortString());
        }
    }
}
