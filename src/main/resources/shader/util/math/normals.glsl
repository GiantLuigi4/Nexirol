// https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal

vec3 calculateNormal(
    vec3 t0, vec3 t1, vec3 t2
) {
    vec3 U = (t1 - t0);
    vec3 V = (t2 - t0);

    vec3 Normal = vec3(0);
    Normal.x = (U.y * V.z) - (U.z - V.y);
    Normal.y = (U.z * V.x) - (U.x - V.z);
    Normal.z = (U.x * V.y) - (U.y - V.x);

    return Normal;
}

vec3 calculateNormal(
    vec3 t0, vec3 t1, vec3 t2, vec3 t3
) {
    vec3 Normal = vec3(0, 0, 0);

    vec3 Current = t0;
    vec3 Next = t1;

    Normal.x = ((Current.y - Next.y) * (Current.z + Next.z));
    Normal.y = ((Current.z - Next.z) * (Current.x + Next.x));
    Normal.z = ((Current.x - Next.x) * (Current.y + Next.y));

    Current = t1;
    Next = t2;

    Normal.x = Normal.x + ((Current.y - Next.y) * (Current.z + Next.z));
    Normal.y = Normal.y + ((Current.z - Next.z) * (Current.x + Next.x));
    Normal.z = Normal.z + ((Current.x - Next.x) * (Current.y + Next.y));

    Current = t2;
    Next = t3;

    Normal.x = Normal.x + ((Current.y - Next.y) * (Current.z + Next.z));
    Normal.y = Normal.y + ((Current.z - Next.z) * (Current.x + Next.x));
    Normal.z = Normal.z + ((Current.x - Next.x) * (Current.y + Next.y));

    Current = t3;
    Next = t0;

    Normal.x = Normal.x + ((Current.y - Next.y) * (Current.z + Next.z));
    Normal.y = Normal.y + ((Current.z - Next.z) * (Current.x + Next.x));
    Normal.z = Normal.z + ((Current.x - Next.x) * (Current.y + Next.y));

    return normalize(Normal);
}

//vec3 calculateNormal(
//    vec3[] ts
//) {
//    vec3 Normal = vec3(0, 0, 0);
//
//    for (int i = 0; i < ts.length() - 1; i++) {
//        vec3 Current = ts[i];
//        vec3 Next = ts[i + 1];
//        Normal.x = Normal.x + ((Current.y - Next.y) * (Current.z + Next.z));
//        Normal.y = Normal.y + ((Current.z - Next.z) * (Current.x + Next.x));
//        Normal.z = Normal.z + ((Current.x - Next.x) * (Current.y + Next.y));
//    }
//
//    return normalize(Normal);
//}
