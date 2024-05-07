// a collection of utils for doing quaternion math on the GPU

/* multiplies two quaternions */
vec4 mul(vec4 q0, vec4 q1) {
    // @formatter:off
    return vec4(
        q0.w * q1.x + q0.x * q1.w + q0.y * q1.z - q0.z * q1.y,
        q0.w * q1.y - q0.x * q1.z + q0.y * q1.w + q0.z * q1.x,
        q0.w * q1.z + q0.x * q1.y - q0.y * q1.x + q0.z * q1.w,
        q0.w * q1.w - q0.x * q1.x - q0.y * q1.y - q0.z * q1.z
    );
    // @formatter:on
}

/* rotates a vector */
vec4 rotate(vec4 point, vec4 quat) {
    float w = point.w;
    point.w = 0;
    point = mul(point, quat);
    quat *= vec4(-1, -1, -1, 1);
    quat = mul(quat, point);
    quat.w = w;
    return quat;
}

/* constructs a quaternion from an axis angle */
vec4 quatFromRotation(float angle, float x, float y, float z) {
    float s = sin(angle * 0.5f);
    x = x * s;
    y = y * s;
    z = z * s;
    float w = cos(angle * 0.5f);
    return vec4(x, y, z, w);
}

/* gets the conjugate of the quaternion */
vec4 conj(vec4 quat) {return vec4(-quat.xyz, quat.w);}
