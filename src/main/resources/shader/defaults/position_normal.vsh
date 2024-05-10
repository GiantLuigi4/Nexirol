#version 450
#extension GL_EXT_scalar_block_layout : enable

// ======= MODEL DATA =======
layout (location = 0) in vec4 Position;
layout (location = 1) in vec3 Normal;

// ======= UNIFORMS =======
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};

// ======= OUTPUT DATA =======
layout (location = 0) out vec4 color;
layout (location = 1) out vec3 wsCoord;
layout (location = 2) out vec3 normal;

// ======= INSTANCE DATA =======
layout (location = 2) in vec3 Offset;
layout (location = 3) in vec4 Rotation;
layout (location = 4) in vec3 Scl;
layout (location = 5) in vec4 ColorMod;

#include <shader/util/quat_math.glsl>

void main() {
    float amt = 1;

    //@formatter:off
    vec4 coord = rotate(Position * vec4(Scl, 1), Rotation)
        + vec4(Offset, 0);
    wsCoord = coord.xyz;
    //@formatter:on

    gl_Position = projectionMatrix * modelViewMatrix * coord;

    vec3 n = Normal;
    n = rotate(vec4(n, 0), (Rotation)).xyz;

//    amt = dot(n * inverse(mat3(modelViewMatrix)), normalize(vec3(0.75, 0.9, -0.5)));
//    amt = dot(n, normalize(vec3(0.75, 0.9, -0.5)));
    color = ColorMod;

    normal = n;
}
