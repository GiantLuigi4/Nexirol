vec4 lerp(
    vec4 c00, vec4 c10,
    vec4 c01, vec4 c11,
    vec2 uv
) {
    vec4 c0 = mix(c00, c01, uv.x);
    vec4 c1 = mix(c10, c11, uv.x);
    vec4 colorDst = mix(c0, c1, uv.y);
    return colorDst;
}

vec3 lerp(
    vec3 c00, vec3 c10,
    vec3 c01, vec3 c11,
    vec2 uv
) {
    vec3 c0 = mix(c00, c01, uv.x);
    vec3 c1 = mix(c10, c11, uv.x);
    vec3 colorDst = mix(c0, c1, uv.y);
    return colorDst;
}

vec2 lerp(
    vec2 c00, vec2 c10,
    vec2 c01, vec2 c11,
    vec2 uv
) {
    vec2 c0 = mix(c00, c01, uv.x);
    vec2 c1 = mix(c10, c11, uv.x);
    vec2 colorDst = mix(c0, c1, uv.y);
    return colorDst;
}

float lerp(
    float c00, float c10,
    float c01, float c11,
    vec2 uv
) {
    float c0 = mix(c00, c01, uv.x);
    float c1 = mix(c10, c11, uv.x);
    float colorDst = mix(c0, c1, uv.y);
    return colorDst;
}