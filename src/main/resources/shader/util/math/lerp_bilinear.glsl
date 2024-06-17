vec4 lerp(
    const vec4 c00, const vec4 c10,
    const vec4 c01, const vec4 c11,
    const vec2 uv
) {
    return mix(mix(c00, c01, uv.x), mix(c10, c11, uv.x), uv.y);
}

vec3 lerp(
    const vec3 c00, const vec3 c10,
    const vec3 c01, const vec3 c11,
    const vec2 uv
) {
    return mix(mix(c00, c01, uv.x), mix(c10, c11, uv.x), uv.y);
}

vec2 lerp(
    const vec2 c00, const vec2 c10,
    const vec2 c01, const vec2 c11,
    const vec2 uv
) {
    return mix(mix(c00, c01, uv.x), mix(c10, c11, uv.x), uv.y);
}

float lerp(
    const float c00, const float c10,
    const float c01, const float c11,
    const vec2 uv
) {
    return mix(mix(c00, c01, uv.x), mix(c10, c11, uv.x), uv.y);
}