// https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal

vec3 calculateNormal(
    const vec3 t0, const vec3 t1, const vec3 t2
) {
    const vec3 U = (t1 - t0);
    const vec3 V = (t2 - t0);
    return normalize(cross(U, V));
}
