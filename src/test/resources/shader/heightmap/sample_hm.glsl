const int VERT =4;
const int GRID =(64*VERT);
const float VERT_SCALE= 3.0;

//#ifndef EXCLUDE_SAMPLE
float sampleHm(vec2 uv) {
    const float height = texture(heightmapSampler, uv).x;
    // TODO: edge implement clamping
    return mix(heightRange.x, heightRange.y, height);
}
//#endif
