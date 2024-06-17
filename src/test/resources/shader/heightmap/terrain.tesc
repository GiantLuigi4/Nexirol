#version 450

// https://learnopengl.com/Guest-Articles/2021/Tessellation/Tessellation

in gl_PerVertex {
    vec4 gl_Position;
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

void main() {
    // ======= VARS =======
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    uvOut[gl_InvocationID] = uv[gl_InvocationID];
    gl_out[gl_InvocationID].gl_Position.y = 0;

    // ======= TESSELATION LEVEL =======
    if (gl_InvocationID == 0) {
        const float MIN_TESS_LEVEL = 1;
        const float MAX_TESS_LEVEL = 64;
        const float MIN_DISTANCE = 64;
        const float MAX_DISTANCE = 2048 * 2;
        const float mm = MAX_DISTANCE - MIN_DISTANCE;

        //const mat3 modelRotation = mat3(modelViewMatrix);
        //const vec3 offset = inverse(modelRotation) * (modelViewMatrix * vec4(0, 0, 0, 1)).xyz;
        // not sure why this works, so I'm keeping the old code as a comment above ^
        const vec3 offset = -(inverse(modelViewMatrix) * vec4(0, 0, 0, 1)).xyz;

        // ----------------------------------------------------------------------
        const vec3 eyeSpacePos00 = gl_in[0].gl_Position.xyz + offset;
        const vec3 eyeSpacePos01 = gl_in[1].gl_Position.xyz + offset;
        const vec3 eyeSpacePos10 = gl_in[2].gl_Position.xyz + offset;
        const vec3 eyeSpacePos11 = gl_in[3].gl_Position.xyz + offset;

        // ----------------------------------------------------------------------
        // calculate horizontal distance of vertex
        const vec4 dist = clamp((abs(vec4(
            length(eyeSpacePos00.xyz),
            length(eyeSpacePos01.xyz),
            length(eyeSpacePos10.xyz),
            length(eyeSpacePos11.xyz)
        )) - MIN_DISTANCE) / mm, 0.0, 1.0);

        // ----------------------------------------------------------------------
        // calculate edge tesselation levels using some simple interpolation
        vec4 tessLevel = mix(MAX_TESS_LEVEL.xxxx, MIN_TESS_LEVEL.xxxx, vec4(
            min(dist.z, dist.x),
            min(dist.x, dist.y),
            min(dist.y, dist.w),
            min(dist.w, dist.z)
        ));

        const mat4 cMat = projectionMatrix * modelViewMatrix;
        vec4 pS0 = cMat * gl_in[0].gl_Position;
        vec4 pS1 = cMat * gl_in[1].gl_Position;
        vec4 pS2 = cMat * gl_in[2].gl_Position;
        vec4 pS3 = cMat * gl_in[3].gl_Position;
        pS0.xyz /= pS0.w;
        pS1.xyz /= pS1.w;
        pS2.xyz /= pS2.w;
        pS3.xyz /= pS3.w;

        // TODO: make this operate on a box defined by these coordinates?
        const vec2 minX = min(min(pS0.xy, pS1.xy), min(pS2.xy, pS3.xy));
        const vec3 maxX = max(max(pS0.xyz, pS1.xyz), max(pS2.xyz, pS3.xyz));
        const float margin = 2.0;
        if (
            maxX.x < -margin ||
            minX.x > margin||
            maxX.y < -margin ||
            minX.y > margin||
            maxX.z < -1.2
        ) tessLevel = vec4(0);

        // ----------------------------------------------------------------------
        // and set
        gl_TessLevelOuter[0] = tessLevel.x;
        gl_TessLevelOuter[1] = tessLevel.y;
        gl_TessLevelOuter[2] = tessLevel.z;
        gl_TessLevelOuter[3] = tessLevel.w;

        gl_TessLevelInner[0] = max(tessLevel.y, tessLevel.w);
        gl_TessLevelInner[1] = max(tessLevel.x, tessLevel.z);
    }
}
