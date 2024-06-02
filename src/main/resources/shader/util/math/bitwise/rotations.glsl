int64_t rotateLeft(int64_t value, int distance) {
    distance &= 63;
    return int64_t(uint64_t(value << distance) | (uint64_t(value) >> (64 - distance)));
}
int64_t rotateRight(int64_t value, int distance) {
    distance &= 63;
    return (value >> distance) | (value << (64u - distance));
}
int rotateLeft(int value, int distance) {
    distance &= 31;
    return int(uint(value << distance) | (uint(value) >> (32 - distance)));
}
int rotateRight(int value, int distance) {
    distance &= 31;
    return (value >> distance) | (value << (32u - distance));
}