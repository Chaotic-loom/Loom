#version 150

// ----------------------------------------------------------------------------
// test_tint.vsh
//
// Vertex shader for Loom's built-in test tint overlay.
//
// This is intentionally minimal — we only need screen-space position.
// No texture coordinates or per-vertex color are needed because the fragment
// shader produces a flat tint colour for the entire quad.
// ----------------------------------------------------------------------------

in vec4 Position;

// Set automatically by RenderSystem from the current model-view and projection
// matrices before BufferUploader.drawWithShader is called.
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

void main() {
    gl_Position = ProjMat * ModelViewMat * Position;
}
