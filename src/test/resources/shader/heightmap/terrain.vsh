#version 450

// ======= MODEL DATA =======
layout (location = 0) in uint vIdx;

// ======= OUTPUT DATA =======
layout (location = 0) out vec3 wsCoord;
layout (location = 1) out vec3 normal;

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

#define GRID 64

void main() {
    const mat4 modelRotation = mat4(mat3(modelViewMatrix));

    ivec2 tSize = textureSize(heightmapSampler, 0);
    vec2 tSizeF = vec2(
        float(tSize.x),
        float(tSize.y)
    );

    // calculate instance position information
    int sizeX = tSize.x / GRID;
    int sizeY = tSize.y / GRID;
    int x = gl_InstanceIndex / sizeX;
    int y = gl_InstanceIndex % sizeX;
    vec2 UVOffset = vec2(x, y);
    x -= sizeX / 2;
    y -= sizeY / 2;
    vec2 POffset = vec2(x, y) * GRID;

    // calculate UV
    uint vX = vIdx / (GRID + 1);
    uint vY = vIdx % (GRID + 1);
    vec2 UV = vec2(
        vX / float(GRID),
        vY / float(GRID)
    );

    // calculate scaled UV
    float uStep = GRID / tSize.x;
    float vStep = GRID / tSize.y;
    vec2 sUV = UV * vec2(uStep, vStep);

    // calculate vertex position information
    vec4 VPosition = vec4(
        UV.x, 0, UV.y, 0
    ) * GRID;
    VPosition.w = 1;


    vec4 vPos = vec4(POffset.x, 0, POffset.y, 1) + VPosition;
    vPos.xz *= 2.;

//    float height = texture(heightmapSampler, (UVOffset / tSizeF * GRID) + sUV).x;
    float height = texture(heightmapSampler, (UVOffset / tSizeF * GRID) + UV / 64 /* TODO: what??? */).x;
//    float height = mix(heightRange.x, heightRange.y, texture(heightmapSampler, UVOffset + UV).x);
    height *= 4.0;

    vPos.y += height * 1000;
    gl_Position = projectionMatrix * modelViewMatrix * vPos;
    wsCoord = vPos.xyz;
    // TODO: calculate normal... or should that be part of the job of the fragment shader?
}
