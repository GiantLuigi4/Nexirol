#version 450

layout (location = 0) in vec3 wsCoord;
layout (location = 1) in vec3 normal;

layout (location = 0) out vec4 colorOut;
layout (location = 1) out vec4 normalOut;

vec4 screenToWorld(mat4 model, mat4 proj, in float depth, in vec2 uv){
    vec4 coord = vec4(uv, depth, 1.0) * 2.0 - 1.0;

    coord = inverse(proj * model) * coord;
    coord /= coord.w; // linearize
    return coord;
}

layout(binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};

// noise
#include <shader/util/noise/hash.glsl>
#include <shader/util/noise/simple.glsl>

void main() {
    normalOut = vec4(normal, 1.0);

    vec3 constantVec = vec3(0.75, 0.9, -0.5) * 300.;
    float amt = dot(normal, abs(normalize(constantVec)));
//    amt = max(0.0, amt);
//    float dAdd = distance(wsCoord.xyz, constantVec);
    float dAdd = 0.;

    float nz = simpleNoise(wsCoord.xz, 1);
    colorOut = vec4((1-nz)/8, nz / 2 + 0.5, nz / 8, 1);
    vec4 grassGreen = vec4(128. / 255., 154. / 255., 128. / 255., 255. / 255.);
    colorOut *= grassGreen;
    colorOut = colorOut * vec4(vec3(amt) * vec3(1-(dAdd / 1000.)), 1.);
}
