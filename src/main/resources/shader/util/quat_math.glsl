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
    float xx = quat.x * quat.x, yy = quat.y * quat.y, zz = quat.z * quat.z, ww = quat.w * quat.w;
    float xy = quat.x * quat.y, xz = quat.x * quat.z, yz = quat.y * quat.z, xw = quat.x * quat.w;
    float zw = quat.z * quat.w, yw = quat.y * quat.w, k = 1 / (xx + yy + zz + ww);
    float x = point.x;
    float y = point.y;
    float z = point.z;
    return vec4(fma((xx - yy - zz + ww) * k, x, fma(2 * (xy - zw) * k, y, (2 * (xz + yw) * k) * z)),
                    fma(2 * (xy + zw) * k, x, fma((yy - xx - zz + ww) * k, y, (2 * (yz - xw) * k) * z)),
                    fma(2 * (xz - yw) * k, x, fma(2 * (yz + xw) * k, y, ((zz - xx - yy + ww) * k) * z)), point.w);
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
