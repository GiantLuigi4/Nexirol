// requires: hash.glsl

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
float simpleNoise(const vec2 p, const float freq) {
    const vec2 ij = floor(p * freq);
    vec2 xy = fract(p * freq);
    xy = xy * xy * (3.0 - 2.0 * xy);

    const float a = rand(round(ij + vec2(0., 0.)));
    const float b = rand(round(ij + vec2(1., 0.)));
    const float c = rand(round(ij + vec2(0., 1.)));
    const float d = rand(round(ij + vec2(1., 1.)));

    return mix(
        mix(a, b, xy.x),
        mix(c, d, xy.x),
        xy.y
    );
}
