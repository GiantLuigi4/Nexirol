// requires: rand.glsl

const float inv = 1 / float(0xFFFFFFFFu);

uint rotl(const uint x, const int k) {
    return (x << k) | (x >> (32 - k));
}

int64_t nextLong(Rand rand) {
    const uint s0 = uint(rand.seedLo);
    uint s1 = uint(rand.seedHi);
    const uint result = s0 + s1;

    s1 ^= s0;
    rand.seedLo = rotl(s0, 7) ^ s1 ^ (s1 << 9); // a, b
    rand.seedHi = rotl(s1, 13); // c

    return int64_t(result);
}
int nextInt(Rand rand) {return int(nextLong(rand));}
int64_t nextBits(Rand rand, int count) {return unsignedRightShift(nextLong(rand), (64 - count));}
float nextFloat(Rand rand) {return float(nextLong(rand)) * inv;}

#undef rotl
#undef inv
