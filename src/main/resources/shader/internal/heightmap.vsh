#version 460

layout (location = 0) in vec2 UV;
layout (location = 0) out vec2 outTerrainUV;
#ifndef NOISE
    layout (location = 1) out vec2 outUV;
#endif

layout (binding = 0) uniform GenData {
    uniform ivec2 regionOffset;
    uniform ivec2 regionSize;
    uniform ivec2 passSize;
};

void main() {
    gl_Position = vec4(UV * 2, 0, 1);

    outTerrainUV = (UV + 0.5) * regionSize + regionOffset;
    outTerrainUV -= passSize / 2;
    #ifndef NOISE
        outUV = UV + 0.5;
    #endif
}
