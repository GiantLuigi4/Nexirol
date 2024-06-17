const int VERT =4;
const int GRID =(64*VERT);
const float VERT_SCALE= 3.0;

//#ifndef EXCLUDE_SAMPLE
//#define ROUGH
float sampleHm(const vec2 uv) {
    // TODO: edge implement clamping
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

float sampleHmNearest(const vec2 uv) {
    // TODO: edge implement clamping
    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 iv = floor(uv * tSize);
    const float height = texelFetch(heightmapSampler, (ivec2(
            int(iv.x),
            int(iv.y)
    ) % tSize), 0).x;
    return mix(heightRange.x, heightRange.y, height);
}
//#endif
