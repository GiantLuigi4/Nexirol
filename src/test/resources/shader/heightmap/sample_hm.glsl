#define VERT 2
#define GRID (32*VERT)
#define VERT_SCALE 3.0

float sampleHm(vec2 uv) {
//    float height = texture(heightmapSampler, (UVOffset / tSizeF * GRID) + sUV).x;
    float height = texture(heightmapSampler, uv).x;
//    float height = mix(heightRange.x, heightRange.y, texture(heightmapSampler, UVOffset + UV).x);
    height *= 4.0;
    height *= 1000;

    return height;
}
