// a collection of utils for doing quaternion math on the GPU

/* multiplies two quaternions */
vec4 mul(const vec4 q0, const vec4 q1) {
    // @formatter:off
    // previous logic
    // return vec4(
    //     q0.w * q1.x + q0.x * q1.w + q0.y * q1.z - q0.z * q1.y,
    //     q0.w * q1.y - q0.x * q1.z + q0.y * q1.w + q0.z * q1.x,
    //     q0.w * q1.z + q0.x * q1.y - q0.y * q1.x + q0.z * q1.w,
    //     q0.w * q1.w - q0.x * q1.x - q0.y * q1.y - q0.z * q1.z
    // );

    return fma(
        q0.wwww,
        q1, fma(
            q0.xxxx,
            vec4(q1.w, -q1.z, q1.y, -q1.x),
            fma(
                q0.yyyy,
                vec4(q1.z, q1.w, -q1.x, -q1.y),
                q0.z * vec4(-q1.y, q1.x, q1.w, -q1.z)
            )
        )
    );
    // @formatter:on
}

/* rotates a vector */
vec4 rotate(const vec4 point, const vec4 quat) {
    const float
    // === squares ===
    xx = quat.x * quat.x,
    yy = quat.y * quat.y,
    zz = quat.z * quat.z,
    ww = quat.w * quat.w,

    // === pairs ===
    xy = quat.x * quat.y,
    xz = quat.x * quat.z,
    yz = quat.y * quat.z,
    xw = quat.x * quat.w,
    zw = quat.z * quat.w,
    yw = quat.y * quat.w,

    // === ks ===
    k = 1.0 / (xx + yy + zz + ww),
    two_k = 2.0 * k;

    // previous logic
    // return vec4(
    //     fma((xx - yy - zz + ww) * k, point.x, fma(two_k * (xy - zw), point.y, (two_k * (xz + yw)) * point.z)),
    //     fma(two_k * (xy + zw), point.x, fma((yy - xx - zz + ww) * k, point.y, (two_k * (yz - xw)) * point.z)),
    //     fma(two_k * (xz - yw), point.x, fma(two_k * (yz + xw), point.y, ((zz - xx - yy + ww) * k) * point.z)),
    //     point.w
    // );

    return vec4(
        fma(
            point.xxx,
            vec3(
                (xx - yy - zz + ww) * k,
                two_k * (xy + zw),
                two_k * (xz - yw)
            ),
            fma(
                point.yyy,
                vec3(
                    two_k * (xy - zw),
                    (yy - xx - zz + ww) * k,
                    two_k * (yz + xw)
                ),
                point.z * vec3(
                    two_k * (xz + yw),
                    two_k * (yz - xw),
                    (zz - xx - yy + ww) * k
                )
            )
        ),
        point.w
    );
}

/* constructs a quaternion from an axis angle */
vec4 quatFromRotation(const float angle, const float x, const float y, const float z) {
    const float s = sin(angle * 0.5);
    const float c = cos(angle * 0.5);
    return vec4(vec3(x, y, z) * s, c);
}

/* gets the conjugate of the quaternion */
vec4 conj(vec4 quat) {return vec4(-quat.xyz, quat.w);}

mat4 quat_matrix(vec4 quat) {
    float w2 = quat.w * quat.w, x2 = quat.x * quat.x;
    float y2 = quat.y * quat.y, z2 = quat.z * quat.z;
    float zw = quat.z * quat.w, dzw = zw + zw, xy = quat.x * quat.y, dxy = xy + xy;
    float xz = quat.x * quat.z, dxz = xz + xz, yw = quat.y * quat.w, dyw = yw + yw;
    float yz = quat.y * quat.z, dyz = yz + yz, xw = quat.x * quat.w, dxw = xw + xw;
    float rm00 = w2 + x2 - z2 - y2;
    float rm01 = dxy + dzw;
    float rm02 = dxz - dyw;
    float rm10 = -dzw + dxy;
    float rm11 = y2 - z2 + w2 - x2;
    float rm12 = dyz + dxw;
    float rm20 = dyw + dxz;
    float rm21 = dyz - dxw;
    float rm22 = z2 - y2 - x2 + w2;
    float nm00 = 1 * rm00 + 0 * rm01 + 0 * rm02;
    float nm01 = 0 * rm00 + 1 * rm01 + 0 * rm02;
    float nm02 = 0 * rm00 + 0 * rm01 + 1 * rm02;
    float nm03 = 1 * rm00 + 1 * rm01 + 1 * rm02;
    float nm10 = 1 * rm10 + 0 * rm11 + 0 * rm12;
    float nm11 = 0 * rm10 + 1 * rm11 + 0 * rm12;
    float nm12 = 0 * rm10 + 0 * rm11 + 1 * rm12;
    float nm13 = 1 * rm10 + 1 * rm11 + 1 * rm12;
    float nm20 = 1 * rm20 + 0 * rm21 + 0 * rm22;
    float nm21 = 0 * rm20 + 1 * rm21 + 0 * rm22;
    float nm22 = 0 * rm20 + 0 * rm21 + 1 * rm22;
    float nm23 = 1 * rm20 + 1 * rm21 + 1 * rm22;
    return mat4(
        nm00, nm01, nm02, nm03,
        nm10, nm11, nm12, nm13,
        nm20, nm21, nm22, nm23,
        0, 0, 0, 1
    );
}
