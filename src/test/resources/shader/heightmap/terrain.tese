#version 450
#extension GL_EXT_scalar_block_layout: enable
#extension GL_EXT_shader_explicit_arithmetic_types: enable

layout (quads, fractional_even_spacing, cw) in;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};
layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;
layout (set = 1, binding = 1) uniform sampler2D heightmapSamplerNearest;

layout (binding = 1) uniform HeightmapData {
    vec2 PositionOffset;
    vec2 heightRange;
};
in gl_PerVertex {
    vec4 gl_Position;
} gl_in[gl_MaxPatchVertices];

// ======= INPUT DATA =======
layout (location = 0) in vec2[] uv;

// ======= FRAGMENT DATA =======
layout (location = 0) out vec3 wsCoordOut;
layout (location = 1) out vec2 uvOut;

// interpolation
#include <shader/util/math/lerp_bilinear.glsl>

// heightmap
#include <shader/heightmap/sample_hm.glsl>

void main() {
    // ======= CONTROL POINTS =======
    const vec2 p00 = gl_in[0].gl_Position.xz;
    const vec2 p01 = gl_in[1].gl_Position.xz;
    const vec2 p10 = gl_in[2].gl_Position.xz;
    const vec2 p11 = gl_in[3].gl_Position.xz;

    // ======= UV =======
    const vec2 uvLerp = lerp(
        uv[0], uv[2],
        uv[1], uv[3],
        gl_TessCoord.xy
    );

    // ======= POSITION =======
    const vec2 p = lerp(
        p00, p10,
        p01, p11,
        gl_TessCoord.xy
    );
    const vec3 offset = -(inverse(modelViewMatrix) * vec4(0, 0, 0, 1)).xyz;
    const vec3 p3 = vec3(
        p,
        sampleHm(uvLerp, length(p + offset.xz) > (2048 * 2))
    ).xzy;

    // ======= OUTPUT VERT =======
    wsCoordOut = p3;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(p3, 1.0);
    uvOut = uvLerp;
}
