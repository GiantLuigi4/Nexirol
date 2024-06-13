#version 450

// ======= MODEL DATA =======
layout (location = 0) in vec4 Position;
layout (location = 1) in vec2 UV;

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
    x -= size / 2;
    y -= size / 2;
    vec2 UVOffset = vec2(x, y);
    vec2 POffset = UVOffset * 64;

    vec4 vPos = vec4(POffset.x, 0, POffset.y, 1) + Position;
    vPos.xz *= 2.;

    float height = texture(heightmapSampler, (UVOffset / tSizeF * 64) + UV).x;
//    float height = 0.0;
//    float height = mix(heightRange.x, heightRange.y, texture(heightmapSampler, UVOffset + UV).x);
    height *= 2.0;

    vPos.y += height * 1000;
    gl_Position = projectionMatrix * modelViewMatrix * vPos;
    wsCoord = vPos.xyz;
    // TODO: calculate normal... or should that be part of the job of the fragment shader?
}
