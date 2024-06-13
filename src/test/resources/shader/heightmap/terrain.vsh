#version 450

// ======= MODEL DATA =======
layout (location = 0) in vec2 UV;

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

void main() {
    const mat4 modelRotation = mat4(mat3(modelViewMatrix));

    ivec2 tSize = textureSize(heightmapSampler, 0);
    vec2 tSizeF = vec2(
        float(tSize.x),
        float(tSize.y)
    );

    // calculate instance position information
    int size = tSize.x / 64;
    int x = gl_InstanceIndex / size;
    int y = gl_InstanceIndex % size;
    vec2 UVOffset = vec2(x, y);
    x -= size / 2;
    y -= size / 2;
    vec2 POffset = vec2(x, y) * 64;

    // calculate vertex position information
    float uStep = 64.0 / tSize.x;
    float vStep = 64.0 / tSize.y;

    vec4 VPosition = vec4(
            UV.x, 0, UV.y, 0
    ) * 64;
    VPosition.w = 1;
    vec2 sUV = UV * vec2(uStep, vStep);


    vec4 vPos = vec4(POffset.x, 0, POffset.y, 1) + VPosition;
    vPos.xz *= 2.;

    float height = texture(heightmapSampler, (UVOffset / tSizeF * 64) + sUV).x;
//    float height = 0.0;
//    float height = mix(heightRange.x, heightRange.y, texture(heightmapSampler, UVOffset + UV).x);
    height *= 2.0;

    vPos.y += height * 1000;
    gl_Position = projectionMatrix * modelViewMatrix * vPos;
    wsCoord = vPos.xyz;
    // TODO: calculate normal... or should that be part of the job of the fragment shader?
}
