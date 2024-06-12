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
        const float MIN_TESS_LEVEL = 16;
        const float MAX_TESS_LEVEL = 64;
        const float MIN_DISTANCE = 1000;
        const float MAX_DISTANCE = 8000;

        const float scale = 1000;

        const mat4 modelRotation = mat4(mat3(modelViewMatrix));

        // ----------------------------------------------------------------------
        // Step 2: transform each vertex into eye space
        vec4 eyeSpacePos00 = modelRotation * ((gl_in[0].gl_Position - vec4(0.5, 0, 0.5, 0) + vec4(
            float(offset[0].x),
            0,
            float(offset[0].y),
            0
        )) * scale);
        vec4 eyeSpacePos01 = modelRotation * ((gl_in[1].gl_Position - vec4(0.5, 0, 0.5, 0) + vec4(
            float(offset[1].x),
            0,
            float(offset[1].y),
            0
        )) * scale);
        vec4 eyeSpacePos10 = modelRotation * ((gl_in[2].gl_Position - vec4(0.5, 0, 0.5, 0) + vec4(
            float(offset[2].x),
            0,
            float(offset[2].y),
            0
        )) * scale);
        vec4 eyeSpacePos11 = modelRotation * ((gl_in[3].gl_Position - vec4(0.5, 0, 0.5, 0) + vec4(
            float(offset[3].x),
            0,
            float(offset[3].y),
            0
        )) * scale);

        // ----------------------------------------------------------------------
        // Step 3: "distance" from camera scaled between 0 and 1
        float distance00 = clamp((abs(eyeSpacePos00.z) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        float distance01 = clamp((abs(eyeSpacePos01.z) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        float distance10 = clamp((abs(eyeSpacePos10.z) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);
        float distance11 = clamp((abs(eyeSpacePos11.z) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE), 0.0, 1.0);

        // ----------------------------------------------------------------------
        // Step 4: interpolate edge tessellation level based on closer vertex
        float tessLevel0 = mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance10, distance00));
        float tessLevel1 = mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance00, distance01));
        float tessLevel2 = mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance01, distance11));
        float tessLevel3 = mix(MAX_TESS_LEVEL, MIN_TESS_LEVEL, min(distance11, distance10));

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

        // TODO: frustum check?
//        eyeSpacePos00 *= projectionMatrix;
//        eyeSpacePos10 *= projectionMatrix;
//        eyeSpacePos01 *= projectionMatrix;
//        eyeSpacePos11 *= projectionMatrix;
//        vec4 pForward = modelRotation * vec4(0, 0, 1, 1);
//        float y = pForward.y * -sign(pForward.z) * sign(pForward.w);
//        if (
//            eyeSpacePos00.w < y * 1000 &&
//            eyeSpacePos10.w < y * 1000 &&
//            eyeSpacePos01.w < y * 1000 &&
//            eyeSpacePos11.w < y * 1000
//        ) {
//            gl_TessLevelInner[0] = 0;
//            gl_TessLevelInner[1] = 0;
//
//            gl_TessLevelOuter[0] = 0;
//            gl_TessLevelOuter[1] = 0;
//            gl_TessLevelOuter[2] = 0;
//            gl_TessLevelOuter[3] = 0;
//        }
    }
}
