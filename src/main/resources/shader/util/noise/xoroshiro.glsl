// requires: rand.glsl
// requires: math/bitwise/rotations.glsl
// requires: math/bitwise/u_shir.glsl

int64_t nextLong(Rand rand) {
    const int64_t i = rand.seedLo;
    int64_t j = rand.seedHi;
    const int64_t k = rotateLeft(i + j, 17) + i;
    j ^= i;
    rand.seedLo = rotateLeft(i, 49) ^ j ^ j << 21;
    rand.seedHi = rotateLeft(j, 28);
    return int64_t(k);
}
int nextInt(Rand rand) {return int(nextLong(rand));}
int64_t nextBits(Rand rand, int count) {return unsignedRightShift(nextLong(rand), (64 - count));}
float nextFloat(Rand rand) {return float(double(nextBits(rand, 24)) * 5.9604645E-8F);}
