vec4 lerp(
    vec4 c00, vec4 c10,
    vec4 c01, vec4 c11,
    vec2 uv
) {
    vec4 c0 = (c01 - c00) * uv.x + c00;
    vec4 c1 = (c11 - c10) * uv.x + c10;
    vec4 colorDst = (c1 - c0) * uv.y + c0;
    return colorDst;
}

vec3 lerp(
    vec3 c00, vec3 c10,
    vec3 c01, vec3 c11,
    vec2 uv
) {
    vec3 c0 = (c01 - c00) * uv.x + c00;
    vec3 c1 = (c11 - c10) * uv.x + c10;
    vec3 colorDst = (c1 - c0) * uv.y + c0;
    return colorDst;
}

vec2 lerp(
    vec2 c00, vec2 c10,
    vec2 c01, vec2 c11,
    vec2 uv
) {
    vec2 c0 = (c01 - c00) * uv.x + c00;
    vec2 c1 = (c11 - c10) * uv.x + c10;
    vec2 colorDst = (c1 - c0) * uv.y + c0;
    return colorDst;
}

float lerp(
    float c00, float c10,
    float c01, float c11,
    vec2 uv
) {
    float c0 = (c01 - c00) * uv.x + c00;
    float c1 = (c11 - c10) * uv.x + c10;
    float colorDst = (c1 - c0) * uv.y + c0;
    return colorDst;
}