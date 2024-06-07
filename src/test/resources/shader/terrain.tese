#version 450
#extension GL_EXT_scalar_block_layout: enable
#extension GL_EXT_shader_explicit_arithmetic_types: enable

layout (quads, fractional_even_spacing, ccw) in;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};

// ======= INPUT DATA =======
layout (location = 0) in ivec2[] offset;

// ======= FRAGMENT DATA =======
layout (location = 0) out vec3 wsCoordOut;
layout (location = 1) out vec3 normalOut;

// interpolation
#include <shader/util/math/lerp_bilinear.glsl>

// noise
#include <shader/util/noise/hash.glsl>
#include <shader/util/noise/simple.glsl>
#include <shader/util/math/bitwise/rotations.glsl>
#include <shader/util/math/bitwise/u_shir.glsl>
#include <shader/util/math/doubles.glsl>
#include <shader/util/noise/rand.glsl>
#include <shader/util/noise/xoroshiro.glsl>
#include <shader/util/noise/perlin_xoro.glsl>

// normals
#include <shader/util/math/normals.glsl>
#line 36

float hm(vec2 pos) {
    vec2 fp = floor(pos);
    if (fp.x == 0 || fp.y == 0) pos += 0.00001;

    float nz = perlinNoise(pos / 10000., 8, vec2(23408, 23472));
    float nz1 = perlinNoise(pos / 20000., 4, vec2(3927423, 7982432));
    nz += nz1 * 2;
    return nz * 1000;
}

vec4 crd(vec4 src, vec2 oset) {
    src.xz += oset;
    src.y = hm(src.xz);
    return src;
}

void main() {
    // get patch coordinate
    float u = gl_TessCoord.x;
    float v = gl_TessCoord.y;
    vec2 uv = vec2(u, v);
    vec4 oGin = transpose(modelViewMatrix) * modelViewMatrix[3].xyzw;

    // ======= CONTROL POINTS =======
    // retrieve control point position coordinates
    vec4 p00 = gl_in[0].gl_Position;
    vec4 p01 = gl_in[1].gl_Position;
    vec4 p10 = gl_in[2].gl_Position;
    vec4 p11 = gl_in[3].gl_Position;

/*
    bool ddiscard = true;
    for (int i = 0; i < 4; i++) {
        vec4 p = lerp(
            p00, p10,
            p01, p11,
            uv
        ) - vec4(0.5, 0, 0.5, 0) + vec4(
            float(offset[0].x),
            0,
            float(offset[0].y),
            0
        );
        vec4 pos = projectionMatrix * modelViewMatrix * p;
        if (pos.w * pos.z > 0) {
            ddiscard = false;
        }
    }
    if (ddiscard) {
        gl_Position = vec4(0);
        return;
    }
*/

    // ======= POSITION =======
    vec4 p = lerp(
        p00, p10,
        p01, p11,
        uv
    ) - vec4(0.5, 0, 0.5, 0) + vec4(
        float(offset[0].x),
        0,
        float(offset[0].y),
        0
    );

    // ======= SIZE & OFFSET =======
    float scl = 1000;
    p.xyz *= scl;
    p.xz -= oGin.xz;
    // ======= SNAP =======
    float step = 1. / scl;
    p.xyz = round(p.xyz / scl * 64) * scl / 64;
    p.xz += oGin.xz;
    p.w = 1.;

    // ======= NOISE =======
    vec4 pset = p;
    pset.xz -= oGin.xz;
    vec4 o_0_0 = crd(pset, vec2(0, 0));
    vec4 o_0p1 = crd(pset, vec2(0, step));
    vec4 op1_0 = crd(pset, vec2(step, 0));
    vec4 o_0n1 = crd(pset, vec2(0, -step));
    vec4 on1_0 = crd(pset, vec2(-step, 0));
    p.y = o_0_0.y;

    vec3 norm = (calculateNormal(
        o_0p1.xyz,
        op1_0.xyz,
        o_0_0.xyz
    ) + calculateNormal(
        o_0_0.xyz,
        o_0n1.xyz,
        on1_0.xyz
    )) * 0.5;
    norm *= sign(norm.y);

    wsCoordOut = p.xyz;
    wsCoordOut.xz -= oGin.xz;

    normalOut = normalize(norm);

    // ======= OUTPUT VERT =======
    gl_Position = projectionMatrix * vec4(mat3(modelViewMatrix) * (p.xyz + vec3(0, oGin.y, 0)), 1);
}
