#version 450

// ======= OUTPUT DATA =======
layout (location = 0) out vec2 uv;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};
layout (binding = 1) uniform HeightmapData {
    uniform vec2 PositionOffset;
    uniform vec2 heightRange;
};
layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;

// heightmap
#define EXCLUDE_SAMPLE
#include <shader/heightmap/sample_hm.glsl>
#undef EXCLUDE_SAMPLE

const float iVert = 1. / VERT;

void main() {
    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 iTSF = 1. / tSize;

    // calculate instance position information
    const ivec2 vSize = tSize / GRID;
    const ivec2 xy = (vSize - ivec2(
        gl_InstanceIndex / vSize.x,
        gl_InstanceIndex % vSize.x
    )) % vSize;
    const vec2 POffset = (xy - vSize * 0.5) * GRID;

    // calculate UV
    const vec2 UV = vec2(
        gl_VertexIndex / (VERT + 1),
        gl_VertexIndex % (VERT + 1)
    ) * iVert;

    // calculate scaled UV
    uv = (xy + UV) * iTSF * GRID;

    // calculate vertex position information
    const vec4 VPosition = vec4(vec3(UV.x, 0, UV.y) * GRID, 1);
    vec4 vPos = vec4(POffset.x, 0, POffset.y, 1) + VPosition;
    vPos.xz *= VERT_SCALE;
    //const float height = sampleHm(uv);
    //vPos.y = height;

    gl_Position = vPos;
}
