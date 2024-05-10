#version 450
#extension GL_EXT_scalar_block_layout : enable

// ======= SHADER OPTIONS =======
#define MAX_INSTANCES 1000

// ======= SHADER START =======
layout (location = 0) in vec4 Position;
layout (location = 1) in vec3 Normal;

//uniform int flags;
layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};

layout (location = 0) out vec4 color;
layout (location = 1) out vec3 wsCoord;
layout (location = 2) out float vDist;

struct Instance {
    vec3 Offset;
    vec4 Rotation;
    vec3 Scl;
    vec4 ColorMod;
};

// @formatter:off
layout (scalar, binding = 1) uniform InstanceData {
    Instance[MAX_INSTANCES] Instances;
};

#include <shader/util/quat_math.glsl>
// @formatter:on

void main() {
    float amt = 1;

    //@formatter:off
    vec4 coord = rotate(Position * vec4(Instances->MultiID.Scl, 1), Instances->MultiID.Rotation)
        + vec4(Instances->MultiID.Offset, 0);
    wsCoord = coord.xyz;
    //@formatter:on

    gl_Position = projectionMatrix * modelViewMatrix * coord;

    vec3 n = Normal;
    n = rotate(vec4(n, 0), (Instances->MultiID.Rotation)).xyz;

//    amt = dot(n * inverse(mat3(modelViewMatrix)), normalize(vec3(0.75, 0.9, -0.5)));
//    amt = dot(n, normalize(vec3(0.75, 0.9, -0.5)));
    amt = dot(n, normalize(vec3(0.75, 0.9, -0.5) * 1. - wsCoord));
    if (amt < 0) amt *= -0.5;
    color = vec4(amt, amt, amt, 1) * Instances->MultiID.ColorMod;

    vDist = length(modelViewMatrix * coord);
}
