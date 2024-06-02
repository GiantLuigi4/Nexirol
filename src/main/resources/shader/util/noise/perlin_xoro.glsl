// requires: hash.glsl
// requires: xoroshiro.glsl

float randXor(vec2 pos, ivec2 seed) {
    pos = floor(pos);
    return nextFloat(reseed(
        Rand(seed.x, seed.y),
        pos.x, 0, pos.y
    ));
}

float simpleXorNoise(vec2 p, float freq, ivec2 seed) {
    vec2 ij = floor(p * freq);
    vec2 xy = fract(p * freq);
    xy = xy * xy * (3.0 - 2.0 * xy);

    float a = randXor((ij + vec2(0., 0.)), seed);
    float b = randXor((ij + vec2(1., 0.)), seed);
    float c = randXor((ij + vec2(0., 1.)), seed);
    float d = randXor((ij + vec2(1., 1.)), seed);

    float x1 = mix(a, b, xy.x);
    float x2 = mix(c, d, xy.x);
    return mix(x1, x2, xy.y);
}

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
float perlinNoise(vec2 p, int res, vec2 seed) {
    const float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    ivec2 iSeed = floatBitsToInt(seed);
    for (int i = 0; i < 50; i++) {
        n += amp * simpleXorNoise(p, f, iSeed);
        f *= 2.;
        normK += amp;
        amp *= persistance;
        if (iCount == res) break;
        iCount++;
    }
    float nf = n / normK;
    return nf * nf * nf * nf;
}
float modifiedPerlinNoise(vec2 p, int res, vec2 seed) {
    const float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    Rand xorSeedRand = reseed(Rand(floatBitsToInt(seed.x), floatBitsToInt(seed.y)), p.x, 0, p.y);
    ivec2 iSeed = floatBitsToInt(seed);
    for (int i = 0; i < 50; i++) {
        n += amp * simpleXorNoise(p, f, iSeed);
        f *= 2.;
        normK += amp;
        amp *= persistance;
        if (iCount == res) break;
        iCount++;
        iSeed.x = nextInt(xorSeedRand);
        iSeed.y = nextInt(xorSeedRand);
    }
    float nf = n / normK;
    return nf * nf * nf * nf;
}
