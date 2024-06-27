#version 450

// ====== OUTPUT DATA ======
layout (location = 0) out vec2 uv;

// ====== UNIFORMS ======
layout (binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};
layout (binding = 1) uniform HeightmapData {
    ivec2 PositionOffset;
    vec2 heightRange;
};
layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;
layout (set = 1, binding = 1) uniform sampler2D heightmapSamplerNearest;

// heightmap
#define EXCLUDE_SAMPLE
#include <shader/heightmap/sample_hm.glsl>
#undef EXCLUDE_SAMPLE

const float iVert = 1. / VERT;
const int V1 = VERT + 1;

out gl_PerVertex {
    vec4 gl_Position;
    float gl_CullDistance[2];
};

// TODO: ideally, the world translates instead of the camera
void main() {
    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 iTSF = 1. / tSize;

    // calculate instance position information
    const ivec2 vSize = tSize / GRID;
    const ivec2 xy = (vSize - ivec2(
        gl_InstanceIndex / vSize.x,
        gl_InstanceIndex % vSize.x
    )) % vSize;
    const vec2 POffset = (xy - vSize * 0.5);

    // calculate UV
    const vec2 UV = vec2(
        gl_VertexIndex / V1,
        gl_VertexIndex % V1
    ) * iVert + PositionOffset / vec2(GRID);

    // calculate scaled UV
    uv = (xy + UV) * iTSF * GRID;

    // calculate vertex position information
    const vec2 vPos = vec2((UV + POffset) * GRID * VERT_SCALE);
    const float height = sampleHmNearest(uv);

    gl_Position = vec4(vPos.x, height, vPos.y, 1.0);

    {
        const vec4 MIN_TESS_LEVEL = vec4(1);
        const vec4 MAX_TESS_LEVEL = vec4(64);
        const float MIN_DISTANCE = 64;
        const float MAX_DISTANCE = 2048 * 2;
        const float mm = 1. / (MAX_DISTANCE - MIN_DISTANCE);

        // ====== CALCULATE VIEW OFFSET ======
        const vec3 offset = -(inverse(modelViewMatrix) * vec4(0, 0, 0, 1)).xyz;
        // ====== OFFSET POSITIONS ======
        const vec3 eyeSpacePos00 = gl_Position.xyz + offset;

        // ====== CALCULATE SS POSITIONS ======
        const mat4 cMat = projectionMatrix * modelViewMatrix;
        vec4 pS0 = cMat * gl_Position;
        pS0.xyz /= pS0.w;

        const float margin = 2.0;
        // ====== CULL ======
        if (
            pS0.x < -margin ||
            pS0.x > margin||
            pS0.y < -margin ||
            pS0.y > margin||
            pS0.z < -1.2 ||
            pS0.w < -0.1
        ) gl_CullDistance[0] = -1;
        else gl_CullDistance[0] = 1;

        // ====== TESS LEVEL ======
        const float dist = clamp((abs(length(eyeSpacePos00)) - MIN_DISTANCE) * mm, 0.0, 1.0);
        gl_CullDistance[1] = dist * gl_CullDistance[0];
    }
}
