#version 450

// https://learnopengl.com/Guest-Articles/2021/Tessellation/Tessellation

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

layout (location = 0) in vec2[] uv;

layout (location = 0) out vec2[] uvOut;

void main() {
    // ======= VARS =======
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    uvOut[gl_InvocationID] = uv[gl_InvocationID];

    // ======= TESSELATION LEVEL =======
    if (gl_InvocationID == 0) {
        const float MIN_TESS_LEVEL = 1;
        const float MAX_TESS_LEVEL = 64;
        const float MIN_DISTANCE = 64;
        const float MAX_DISTANCE = 2048 * 2;

        const mat4 modelRotation = mat4(mat3(modelViewMatrix));
        vec4 offset = modelViewMatrix * vec4(0, 0, 0, 1);
        offset.y = 0;

        // ----------------------------------------------------------------------
        const vec4 eyeSpacePos00 = (modelRotation * gl_in[0].gl_Position) + offset;
        const vec4 eyeSpacePos01 = (modelRotation * gl_in[1].gl_Position) + offset;
        const vec4 eyeSpacePos10 = (modelRotation * gl_in[2].gl_Position) + offset;
        const vec4 eyeSpacePos11 = (modelRotation * gl_in[3].gl_Position) + offset;

        // ----------------------------------------------------------------------
        // calculate horizontal distance of vertex
        const float distance00 = clamp((abs(length(eyeSpacePos00.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        const float distance01 = clamp((abs(length(eyeSpacePos01.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        const float distance10 = clamp((abs(length(eyeSpacePos10.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        const float distance11 = clamp((abs(length(eyeSpacePos11.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);

        // ----------------------------------------------------------------------
        // calculate edge tesselation levels using some simple interpolation
        // clamp to the tesselation levels, because elsewise this will reach 0
        const float tessLevel0 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance10, distance00)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );
        const float tessLevel1 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance00, distance01)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );
        const float tessLevel2 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance01, distance11)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );
        const float tessLevel3 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance11, distance10)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );

        // ----------------------------------------------------------------------
        // and set
        gl_TessLevelOuter[0] = tessLevel0;
        gl_TessLevelOuter[1] = tessLevel1;
        gl_TessLevelOuter[2] = tessLevel2;
        gl_TessLevelOuter[3] = tessLevel3;

        gl_TessLevelInner[0] = max(tessLevel1, tessLevel3);
        gl_TessLevelInner[1] = max(tessLevel0, tessLevel2);
    }
}
