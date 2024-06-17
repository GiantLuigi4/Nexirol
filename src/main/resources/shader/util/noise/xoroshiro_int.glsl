// requires: rand.glsl
// requires: math/bitwise/rotations.glsl
// requires: math/bitwise/u_shir.glsl

int64_t nextLong(Rand rand) {
    const int i = int(rand.seedLo - rand.seedHi);
    int j = int(rand.seedHi ^ rand.seedLo);
    const int k = rotateLeft(i + j, 17) + i;
    j ^= i;
    rand.seedLo = rotateLeft(i, 15) ^ j ^ j << 13;
    rand.seedHi = rotateLeft(j, 13);
    return int64_t(k);
}
int nextInt(Rand rand) {return int(nextLong(rand));}
int64_t nextBits(Rand rand, int count) {return unsignedRightShift(nextLong(rand), (64 - count));}
float nextFloat(Rand rand) {return float(double(nextBits(rand, 24)) * 5.9604645E-8F);}
