// requires: hash.glsl

// https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83#perlin-noise
#define PI 3.14159265358
float _simple_noise_random_(vec2 coord, vec2 seed) {
    coord = ceil(coord + vec2(rand(seed.xy) * rand(seed.xx) * 1000, rand(seed.yx) * rand(seed.yy) * 1000));
    return rand(vec2(
        randI(ivec2(int(coord.x), int(coord.y))),
        randI(ivec2(int(coord.y), int(coord.x)))
    ));
}

float simpleNoise(vec2 p, float freq, vec2 seed) {
    float unit = 1.0 / freq;
    vec2 ij = floor(p / unit);
    vec2 xy = mod(p, unit) / unit;
    xy = 3. * xy * xy - 2. * xy * xy * xy;
    float a = _simple_noise_random_((ij + vec2(0., 0.)), seed);
    float b = _simple_noise_random_((ij + vec2(1., 0.)), seed);
    float c = _simple_noise_random_((ij + vec2(0., 1.)), seed);
    float d = _simple_noise_random_((ij + vec2(1., 1.)), seed);
    float x1 = mix(a, b, xy.x);
    float x2 = mix(c, d, xy.x);
    return mix(x1, x2, xy.y);
}
#undef PI
