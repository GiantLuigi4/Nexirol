// requires: hash.glsl
// requires: simple.glsl

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
float perlinNoise(vec2 p, int res, vec2 seed) {
    float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    for (int i = 0; i < 50; i++) {
        n += amp * simpleNoise(p, f, seed);
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
    float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    for (int i = 0; i < 50; i++) {
        n += amp * simpleNoise(p, f, seed);
        f *= 2.;
        normK += amp;
        amp *= persistance;
        if (iCount == res) break;
        iCount++;
        seed = vec2(
                rand(seed.xy) * rand(seed.xx) * seed.x * 100 * (i + 1),
                rand(seed.yx) * rand(seed.yy) * seed.y * 100 * (i + 1)
        );
    }
    float nf = n / normK;
    return nf * nf * nf * nf;
}
