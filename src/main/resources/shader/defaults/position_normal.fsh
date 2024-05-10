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

    float amt = dot(normal, normalize(vec3(0.75, 0.9, -0.5) * 300. - wsCoord));
    if (amt < 0) amt *= 0;
    colorOut = vec4(amt, amt, amt, 1) * color;

    float dAdd = distance(wsCoord.xyz, vec3(0.75, 0.9, -0.5) * 300.);
    colorOut = vec4(1-(dAdd / 1000.)) * colorOut;
}
