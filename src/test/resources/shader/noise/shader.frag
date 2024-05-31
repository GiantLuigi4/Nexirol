#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

#include <shader/util/noise/hash.glsl>
#include <shader/util/noise/simple.glsl>
#include <shader/util/noise/perlin.glsl>

void main() {
    outColor = vec4(fragColor.xy, 0, 1);

    float nz = modifiedPerlinNoise(fragColor.xy * 10., 8, vec2(23408, 23472));
    nz += modifiedPerlinNoise(fragColor.xy * 10., 8, vec2(879439, 839274));

    outColor = vec4(vec3(nz), 1);
}
