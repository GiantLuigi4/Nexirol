int64_t unsignedRightShift(int64_t value, int distance) {
    uint64_t unsignedValue = uint64_t(value);
    uint64_t shifted = unsignedValue >> uint64_t(distance);
    return int64_t(shifted);
}
int unsignedRightShift(int value, int distance) {
    uint unsignedValue = uint(value);
    uint shifted = unsignedValue >> uint(distance);
    return int(shifted);
}
