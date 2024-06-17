struct Rand {
    int64_t seedLo;
    int64_t seedHi;
};

int64_t seed(const int x, const int y, const int z) {
    int64_t i = int64_t(x * 3129871) ^ int64_t(z * 116129781) ^ int64_t(y);
    i = i * i * 42317861 + i * 11;
    return i >> 16;
}
Rand randFor(const Rand rng, const int x, const int y, const int z) {
    int64_t seed = seed(x, y, z);
    int64_t j = seed ^ rng.seedLo;
    return Rand(j, rng.seedHi);
}
Rand randFor(const Rand rng, const float x, const float y, const float z) {
    int64_t seed = seed(floatBitsToInt(x), floatBitsToInt(y), floatBitsToInt(z));
    int64_t j = seed ^ rng.seedLo;
    return Rand(j, rng.seedHi);
}

Rand reseed(Rand rng, const int x, const int y, const int z) {
    int64_t seed = seed(x, y, z);
    rng.seedLo = seed ^ rng.seedLo;
    return rng;
}
Rand reseed(Rand rng, const float x, const float y, const float z) {
    int64_t seed = seed(floatBitsToInt(x), floatBitsToInt(y), floatBitsToInt(z));
    rng.seedLo = seed ^ rng.seedLo;
    return rng;
}
