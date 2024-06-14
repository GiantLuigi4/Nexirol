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
    // get patch coordinate
    const float u = gl_TessCoord.x;
    const float v = gl_TessCoord.y;
    const vec2 uvC = vec2(u, v);

    // ======= CONTROL POINTS =======
    // retrieve control point position coordinates
    const vec3 p00 = gl_in[0].gl_Position.xyz;
    const vec3 p01 = gl_in[1].gl_Position.xyz;
    const vec3 p10 = gl_in[2].gl_Position.xyz;
    const vec3 p11 = gl_in[3].gl_Position.xyz;

    // ======= POSITION =======
    vec4 p = vec4(lerp(
        p00, p10,
        p01, p11,
        uvC
    ), 1);

    const vec2 uvLerp = lerp(
        uv[0], uv[2],
        uv[1], uv[3],
        uvC
    );
    p.y += sampleHm(uvLerp);

    wsCoordOut = p.xyz;

    // ======= OUTPUT VERT =======
    gl_Position = projectionMatrix * modelViewMatrix * p;
    wsCoordOut = p.xyz;
    uvOut = uvLerp;
}
