int64_t unsignedRightShift(const int64_t value, const int distance) {
    const uint64_t unsignedValue = uint64_t(value);
    const uint64_t shifted = unsignedValue >> uint64_t(distance);
    return int64_t(shifted);
}
int unsignedRightShift(const int value, const int distance) {
    const uint unsignedValue = uint(value);
    const uint shifted = unsignedValue >> uint(distance);
    return int(shifted);
}
