struct Rand {
    int64_t seedLo;
    int64_t seedHi;
};

int64_t seed(int x, int y, int z) {
    int64_t i = int64_t(x * 3129871) ^ int64_t(z * 116129781) ^ int64_t(y);
    i = i * i * 42317861 + i * 11;
    return i >> 16;
}
Rand randFor(Rand rng, int x, int y, int z) {
    int64_t seed = seed(x, y, z);
    int64_t j = seed ^ rng.seedLo;
    return Rand(j, rng.seedHi);
}
Rand randFor(Rand rng, float x, float y, float z) {
    int64_t seed = seed(floatBitsToInt(x), floatBitsToInt(y), floatBitsToInt(z));
    int64_t j = seed ^ rng.seedLo;
    return Rand(j, rng.seedHi);
}

Rand reseed(Rand rng, int x, int y, int z) {
    int64_t seed = seed(x, y, z);
    rng.seedLo = seed ^ rng.seedLo;
    return rng;
}
Rand reseed(Rand rng, float x, float y, float z) {
    int64_t seed = seed(floatBitsToInt(x), floatBitsToInt(y), floatBitsToInt(z));
    rng.seedLo = seed ^ rng.seedLo;
    return rng;
}
