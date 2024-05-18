#version 450

layout (location = 0) in vec4 color;
layout (location = 1) in vec3 wsCoord;
layout (location = 2) in vec3 normal;

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

void main() {
    normalOut = vec4(normal, 1.0);

    vec3 constantVec = vec3(0.75, 0.9, -0.5) * 300.;
    float amt = dot(normal, normalize(constantVec - wsCoord));
    amt = max(0.0, amt);
    float dAdd = distance(wsCoord.xyz, constantVec);
    colorOut = vec4(vec3(amt) * color.xyz * vec3(1-(dAdd / 1000.)), color.a);
}
