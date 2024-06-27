#version 450

// https://learnopengl.com/Guest-Articles/2021/Tessellation/Tessellation

in gl_PerVertex {
    vec4 gl_Position;
    float gl_CullDistance[2];
} gl_in[gl_MaxPatchVertices];
out gl_PerVertex {
    vec4 gl_Position;
} gl_out[];

layout (binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};

layout (vertices = 4) out;

layout (location = 0) in vec2[] uv;

layout (location = 0) out vec2[] uvOut;
layout (location = 1) out vec3[] oSet;

void main() {
    // ====== VARS ======
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    gl_out[gl_InvocationID].gl_Position.y = 0;
    uvOut[gl_InvocationID] = uv[gl_InvocationID];

    // ====== TESSELATION LEVEL ======
    if (gl_InvocationID == 0) {
        // ====== CALCULATE VIEW OFFSET ======
        //const mat3 modelRotation = mat3(modelViewMatrix);
        //const vec3 offset = inverse(modelRotation) * (modelViewMatrix * vec4(0, 0, 0, 1)).xyz;
        // not sure why this works, so I'm keeping the old code as a comment above ^
        const vec3 offset = -(inverse(modelViewMatrix) * vec4(0, 0, 0, 1)).xyz;
        oSet[gl_InvocationID] = offset;

        const float max0 = max(
            max(
                gl_in[0].gl_CullDistance[0],
                gl_in[1].gl_CullDistance[0]
            ),
            max(
                gl_in[2].gl_CullDistance[0],
                gl_in[3].gl_CullDistance[0]
            )
        );
        const vec4 tessLevel = vec4(
                abs(gl_in[0].gl_CullDistance[1]) * max0,
                abs(gl_in[1].gl_CullDistance[1]) * max0,
                abs(gl_in[2].gl_CullDistance[1]) * max0,
                abs(gl_in[3].gl_CullDistance[1]) * max0
        );

        // ====== OUTPUT ======
        gl_TessLevelOuter[0] = tessLevel.x;
        gl_TessLevelOuter[1] = tessLevel.y;
        gl_TessLevelOuter[2] = tessLevel.z;
        gl_TessLevelOuter[3] = tessLevel.w;

        gl_TessLevelInner[0] = max(tessLevel.y, tessLevel.w);
        gl_TessLevelInner[1] = max(tessLevel.x, tessLevel.z);
    }
}
