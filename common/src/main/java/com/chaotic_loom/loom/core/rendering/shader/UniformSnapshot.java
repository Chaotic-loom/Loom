package com.chaotic_loom.loom.core.rendering.shader;

import com.mojang.blaze3d.shaders.Uniform;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * An immutable snapshot of a {@link Uniform}'s value at parse time.
 * <p>
 * Captured once when the shader JSON is loaded; applied whenever
 * {@link ManagedShaderInstance#restoreUniformDefaults()} is called.
 * Keeping the snapshot as a proper value type (rather than a raw Consumer)
 * makes serialization, debugging, and copying straightforward.
 */
public sealed interface UniformSnapshot permits UniformSnapshot.IntSnapshot, UniformSnapshot.FloatSnapshot {

    /** Write the snapshot back into {@code target}. */
    void applyTo(Uniform target);

    // -------------------------------------------------------------------------
    // Integer uniforms  (type index 0–3 in Blaze3D)
    // -------------------------------------------------------------------------

    record IntSnapshot(int[] values) implements UniformSnapshot {

        /** Defensive copy so callers cannot mutate the snapshot after creation. */
        public IntSnapshot(int[] values) {
            this.values = values.clone();
        }

        public static IntSnapshot capture(Uniform uniform) {
            IntBuffer buf = uniform.getIntBuffer();
            int count = uniform.getCount();
            int[] captured = new int[count];
            buf.position(0);
            for (int i = 0; i < count; i++) {
                captured[i] = buf.get(i);
            }
            return new IntSnapshot(captured);
        }

        @Override
        public void applyTo(Uniform target) {
            IntBuffer buf = target.getIntBuffer();
            buf.position(0);
            buf.put(values);
        }
    }

    // -------------------------------------------------------------------------
    // Float uniforms  (type index 4+ in Blaze3D)
    // -------------------------------------------------------------------------

    record FloatSnapshot(float[] values) implements UniformSnapshot {

        /** Defensive copy so callers cannot mutate the snapshot after creation. */
        public FloatSnapshot(float[] values) {
            this.values = values.clone();
        }

        public static FloatSnapshot capture(Uniform uniform) {
            FloatBuffer buf = uniform.getFloatBuffer();
            int count = uniform.getCount();
            float[] captured = new float[count];
            buf.position(0);
            for (int i = 0; i < count; i++) {
                captured[i] = buf.get(i);
            }
            return new FloatSnapshot(captured);
        }

        @Override
        public void applyTo(Uniform target) {
            FloatBuffer buf = target.getFloatBuffer();
            buf.position(0);
            buf.put(values);
        }
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Captures the current value of {@code uniform}, choosing the correct
     * snapshot subtype based on Blaze3D's internal type encoding.
     *
     * @param uniform the uniform to snapshot
     * @return an {@link IntSnapshot} for integer uniforms, {@link FloatSnapshot} otherwise
     */
    static UniformSnapshot capture(Uniform uniform) {
        // Blaze3D encodes int uniforms as type 0–3, floats as 4+
        if (uniform.getType() <= 3) {
            return IntSnapshot.capture(uniform);
        }
        return FloatSnapshot.capture(uniform);
    }
}