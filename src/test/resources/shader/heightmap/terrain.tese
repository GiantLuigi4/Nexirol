#version 450
#extension GL_EXT_scalar_block_layout: enable
#extension GL_EXT_shader_explicit_arithmetic_types: enable

layout (quads, fractional_even_spacing, cw) in;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};
layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;

layout (binding = 1) uniform HeightmapData {
    uniform vec2 PositionOffset;
    uniform vec2 heightRange;
};

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
    const vec3 p00 = gl_in[0].gl_Position.xyz;
    const vec3 p01 = gl_in[1].gl_Position.xyz;
    const vec3 p10 = gl_in[2].gl_Position.xyz;
    const vec3 p11 = gl_in[3].gl_Position.xyz;

    // ======= POSITION =======
    vec3 p = lerp(
        p00, p10,
        p01, p11,
        gl_TessCoord.xy
    );

    // ======= UV =======
    const vec2 uvLerp = lerp(
        uv[0], uv[2],
        uv[1], uv[3],
        gl_TessCoord.xy
    );
    p.y = sampleHm(uvLerp);


    // ======= OUTPUT VERT =======
    wsCoordOut = p;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(p, 1.0);
    uvOut = uvLerp;
}
