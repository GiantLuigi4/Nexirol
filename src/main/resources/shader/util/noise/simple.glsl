// requires: hash.glsl

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
float simpleNoise(vec2 p, float freq) {
    vec2 ij = floor(p * freq);
    vec2 xy = fract(p * freq);
    xy = xy * xy * (3.0 - 2.0 * xy);

    float a = rand(round(ij + vec2(0., 0.)));
    float b = rand(round(ij + vec2(1., 0.)));
    float c = rand(round(ij + vec2(0., 1.)));
    float d = rand(round(ij + vec2(1., 1.)));

    float x1 = mix(a, b, xy.x);
    float x2 = mix(c, d, xy.x);
    return mix(x1, x2, xy.y);
}
