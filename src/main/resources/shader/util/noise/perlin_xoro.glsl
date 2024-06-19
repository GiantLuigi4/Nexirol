// requires: hash.glsl
// requires: xoroshiro.glsl

#ifndef MAX_LOOPS
#define MAX_LOOPS 8
#endif

float randXor(vec2 pos, const ivec2 seed) {
    pos = floor(pos);
    return nextFloat(reseed(
        Rand(seed.x, seed.y),
        pos.x, 0, pos.y
    ));
}

float simpleXorNoise(const vec2 p, const float freq, const ivec2 seed) {
    const vec2 ij = floor(p * freq);
    vec2 xy = fract(p * freq);
    xy = xy * xy * (3.0 - 2.0 * xy);

    const float a = randXor((ij + vec2(0., 0.)), seed);
    const float b = randXor((ij + vec2(1., 0.)), seed);
    const float c = randXor((ij + vec2(0., 1.)), seed);
    const float d = randXor((ij + vec2(1., 1.)), seed);

    return mix(
        mix(a, b, xy.x),
        mix(c, d, xy.x),
        xy.y
    );
}

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
float perlinNoise(const vec2 p, const int res, const vec2 seed) {
    const float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    const ivec2 iSeed = floatBitsToInt(seed);
    for (int i = 0; i < 50; i++) {
        n += amp * simpleXorNoise(p, f, iSeed);
        f *= 2.;
        normK += amp;
        amp *= persistance;
        if (iCount == res) break;
        iCount++;
    }
    const float nf = n / normK;
    return nf * nf * nf * nf;
}
float modifiedPerlinNoise(const vec2 p, const int res, const vec2 seed) {
    const float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    const Rand xorSeedRand = reseed(Rand(floatBitsToInt(seed.x), floatBitsToInt(seed.y)), p.x, 0, p.y);
    ivec2 iSeed = floatBitsToInt(seed);
    for (int i = 0; i < MAX_LOOPS; i++) {
        n += amp * simpleXorNoise(p, f, iSeed);
        f *= 2.;
        normK += amp;
        amp *= persistance;
        if (iCount == res) break;
        iCount++;
        iSeed.x = nextInt(xorSeedRand);
        iSeed.y = nextInt(xorSeedRand);
    }
    const float nf = n / normK;
    return nf * nf * nf * nf;
}

#undef MAX_LOOPS
