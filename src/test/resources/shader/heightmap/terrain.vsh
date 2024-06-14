#version 450

// ======= OUTPUT DATA =======
layout (location = 0) out vec2 uv;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};
layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;

//layout (binding = 1) uniform HeightmapData {
//    uniform vec2 UVOffset;
//    uniform vec2 heightRange;
//};

// heightmap
#include <shader/heightmap/sample_hm.glsl>

void main() {
    const mat4 modelRotation = mat4(mat3(modelViewMatrix));

    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 tSizeF = vec2(
        float(tSize.x),
        float(tSize.y)
    );

    // calculate instance position information
    const int sizeX = tSize.x / GRID;
    const int sizeY = tSize.y / GRID;
    int x = gl_InstanceIndex / sizeX;
    int y = gl_InstanceIndex % sizeX;
    const vec2 UVOffset = vec2(x, y);
    x -= sizeX / 2;
    y -= sizeY / 2;
    const vec2 POffset = vec2(x, y) * GRID;

    // calculate UV
    const uint vX = gl_VertexIndex / (VERT + 1);
    const uint vY = gl_VertexIndex % (VERT + 1);
    const vec2 UV = vec2(
        vX / float(GRID * VERT),
        vY / float(GRID * VERT)
    ) * GRID;

    // calculate vertex position information
    vec4 VPosition = vec4(
        UV.x, 0, UV.y, 0
    ) * GRID;
    VPosition.w = 1;


    vec4 vPos = vec4(POffset.x, 0, POffset.y, 1) + VPosition;
    vPos.xz *= VERT_SCALE;

    // calculate scaled UV
    const vec2 sUV = UV * (GRID / tSizeF);
    uv = (UVOffset / tSizeF * GRID) + sUV;

    const float height = sampleHm(uv);
    vPos.y += height;

    gl_Position = vPos;
}
