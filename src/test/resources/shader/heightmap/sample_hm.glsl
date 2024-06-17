const int VERT = 4;
const int GRID = (64*VERT);
const float VERT_SCALE= 3.0;

//#define ROUGH

//#ifndef EXCLUDE_SAMPLE
vec2 clampToEdge(vec2 uv) {
    // TODO: implement edge clamping
//    uv = mod(uv, 1);
//    const vec2 tSize = textureSize(heightmapSampler, 0);
//    const vec2 pSet = PositionOffset / 2;
//    uv -= pSet;
//    uv = clamp(uv, 0.25, 0.75);
//    uv += pSet;
    return uv;
}

float sampleHm(vec2 uv) {
    uv = clampToEdge(uv);
    #ifndef ROUGH
        const float height = textureLod(heightmapSampler, uv, 0).x;
    #else
        const ivec2 tSize = textureSize(heightmapSampler, 0);
        const vec2 iv = floor(uv * tSize);
        const float height = texelFetch(heightmapSampler, (ivec2(
                int(iv.x),
                int(iv.y)
        ) % tSize), 0).x;
    #endif
    return mix(heightRange.x, heightRange.y, height);
}

float sampleHmNearest(vec2 uv) {
    uv = clampToEdge(uv);
    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 iv = floor(uv * tSize);
    const float height = texelFetch(heightmapSampler, (ivec2(
            int(iv.x),
            int(iv.y)
    ) % tSize), 0).x;
    return mix(heightRange.x, heightRange.y, height);
}
//#endif
