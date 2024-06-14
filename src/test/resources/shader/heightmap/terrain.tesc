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

layout (location = 0) in vec2[] uv;

layout (location = 0) out vec2[] uvOut;

void main() {
    // ======= VARS =======
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    uvOut[gl_InvocationID] = uv[gl_InvocationID];

    // ======= TESSELATION LEVEL =======
    if (gl_InvocationID == 0) {
        const float MIN_TESS_LEVEL = 4;
        const float MAX_TESS_LEVEL = 32;
        const float MIN_DISTANCE = 64;
        const float MAX_DISTANCE = 256 * 2;

        const mat4 modelRotation = modelViewMatrix;

        // ----------------------------------------------------------------------
        // Step 2: transform each vertex into eye space
        vec4 eyeSpacePos00 = modelRotation * gl_in[0].gl_Position;
        vec4 eyeSpacePos01 = modelRotation * gl_in[1].gl_Position;
        vec4 eyeSpacePos10 = modelRotation * gl_in[2].gl_Position;
        vec4 eyeSpacePos11 = modelRotation * gl_in[3].gl_Position;

        // ----------------------------------------------------------------------
        // Step 3: "distance" from camera scaled between 0 and 1
        float distance00 = clamp((abs(length(eyeSpacePos00.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        float distance01 = clamp((abs(length(eyeSpacePos01.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        float distance10 = clamp((abs(length(eyeSpacePos10.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        float distance11 = clamp((abs(length(eyeSpacePos11.xz)) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);

        // ----------------------------------------------------------------------
        // Step 4: interpolate edge tessellation level based on closer vertex
        float tessLevel0 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance10, distance00)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );
        float tessLevel1 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance00, distance01)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );
        float tessLevel2 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance01, distance11)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );
        float tessLevel3 = clamp(
            mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance11, distance10)),
            MIN_TESS_LEVEL,
            MAX_TESS_LEVEL
        );

        // ----------------------------------------------------------------------
        // Step 5: set the corresponding outer edge tessellation levels
        gl_TessLevelOuter[0] = tessLevel0;
        gl_TessLevelOuter[1] = tessLevel1;
        gl_TessLevelOuter[2] = tessLevel2;
        gl_TessLevelOuter[3] = tessLevel3;

        // ----------------------------------------------------------------------
        // Step 6: set the inner tessellation levels to the max of the two parallel edges
        gl_TessLevelInner[0] = max(tessLevel1, tessLevel3);
        gl_TessLevelInner[1] = max(tessLevel0, tessLevel2);
    }
}
