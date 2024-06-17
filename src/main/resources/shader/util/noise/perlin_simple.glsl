// requires: hash.glsl
// requires: simple.glsl

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
float perlinNoise(const vec2 p, const int res) {
    const float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    for (int i = 0; i < 50; i++) {
        n += amp * simpleNoise(p, f);
        f *= 2.;
        normK += amp;
        amp *= persistance;
        if (iCount == res) break;
        iCount++;
    }
    const float nf = n / normK;
    return nf * nf * nf * nf;
}
