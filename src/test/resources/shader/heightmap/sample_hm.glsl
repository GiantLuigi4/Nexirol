#define VERT 4
#define GRID (64*VERT)
#define VERT_SCALE 3.0

//#ifndef EXCLUDE_SAMPLE
float sampleHm(vec2 uv) {
    float height = texture(heightmapSampler, uv).x;
    height = mix(heightRange.x, heightRange.y, height);
    // TODO: implement clamping
    return height;
}
//#endif
