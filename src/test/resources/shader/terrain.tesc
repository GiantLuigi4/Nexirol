#version 450

in gl_PerVertex {
    vec4 gl_Position;
    float gl_PointSize;
    float[] gl_ClipDistance;
    float[] gl_CullDistance;
} gl_in[gl_MaxPatchVertices];

layout (binding = 0) uniform Matrices {
    uniform mat4 projectionMatrix;
    uniform mat4 modelViewMatrix;
};

layout (vertices = 4) out;

layout (location = 0) in ivec2[] offset;

layout (location = 0) out ivec2[] offsetOut;

void main() {
    // ======= VARS =======
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    offsetOut[gl_InvocationID] = offset[gl_InvocationID];

    // ======= TESSELATION LEVEL =======
    if (gl_InvocationID == 0) {
        int level = 64;

        gl_TessLevelOuter[0] = level;
        gl_TessLevelOuter[1] = level;
        gl_TessLevelOuter[2] = level;
        gl_TessLevelOuter[3] = level;

        gl_TessLevelInner[0] = level;
        gl_TessLevelInner[1] = level;
    }
}