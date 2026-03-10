#version 150

// ----------------------------------------------------------------------------
// test_tint.fsh
//
// Fragment shader for Loom's built-in test tint overlay.
//
// Outputs a flat colour derived from two uniforms:
//   Tint      — the base RGBA colour of the tint (set per-frame by Java code)
//   Intensity — a scalar multiplier applied to the alpha channel
//
// Because alpha blending is enabled on the draw call (blend func: src_alpha,
// 1-src_alpha), the result is a translucent colour wash over whatever was
// already on screen when this quad is drawn.
//
// To experiment:
//   - Change Tint to (0.0, 0.0, 0.0, 0.8) for a dark fade-to-black effect
//   - Change Tint to (1.0, 1.0, 1.0, 0.2) for a subtle white flash
//   - Animate Intensity from Java to create a pulsing effect
// ----------------------------------------------------------------------------

// The RGBA tint colour. Alpha channel controls how opaque the overlay is.
// Default value (defined in test_tint.json) is a semi-transparent red.
uniform vec4  Tint;

// Multiplied against Tint.a before output. Lets callers dim the whole effect
// without touching the tint colour itself. Default value is 1.0 (no scaling).
uniform float Intensity;

out vec4 fragColor;

void main() {
    // The RGB channels come directly from Tint.
    // The alpha is Tint.a scaled by Intensity, clamped to [0, 1].
    fragColor = vec4(Tint.rgb, clamp(Tint.a * Intensity, 0.0, 1.0));
}
