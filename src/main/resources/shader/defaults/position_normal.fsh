#version 450

layout (location = 0) in vec4 color;
layout (location = 1) in vec3 wsCoord;
layout (location = 2) in float vDist;

layout (location = 0) out vec4 colorOut;

void main() {
    colorOut = color;

    float dAdd = distance(wsCoord, vec3(0.75, 0.9, -0.5) * 1.);
    colorOut = vec4(1-(dAdd / 100.)) * color;

    // TODO: fog
}
