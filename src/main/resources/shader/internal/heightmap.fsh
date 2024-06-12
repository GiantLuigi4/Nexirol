
layout(location = 0) in vec2 terrainUV;
#ifndef NOISE
    layout(location = 1) in vec2 uv;
#endif

#ifdef HIGHP
    #ifdef MODE_ALPHA
        layout(location = 0) out vec4 col;
    #else
        layout(location = 0) out vec3 col;
    #endif
#else
    #ifdef MODE_ALPHA
        layout(location = 0) out vec4 col;
    #else
        layout(location = 0) out float col;
    #endif
#endif

void main() {
    #ifdef NOISE
        nexi_In.terrainUV = ivec2(int(terrainUV.x), int(terrainUV.y));
    #else
        nexi_In.terrainUV = ivec2(int(terrainUV.x), int(terrainUV.y));
        nexi_In.texUV = uv;
    #endif

    float value;
    float alpha;
    #ifdef MODE_ALPHA
        #ifndef NOISE
            vec2 vvalue = ENTRY();
            value = vvalue.x;
            alpha = vvalue.y;
        #else
            value = ENTRY();
            alpha = 1.0;
        #endif
    #else
        value = ENTRY();
        alpha = 1.0;
    #endif

    #ifdef HIGHP
        int ivalue = int(value * 16777215); // 16777215 is the largest value representable with 3 8-bit integers

        ivec3 intVec;
        intVec.x = (ivalue >> 16) & 255;
        intVec.y = (ivalue >> 8) & 255;
        intVec.z = ivalue & 255;

//        int reconIVal =
//            (intVec.x << 16) |
//            (intVec.y << 8) |
//            (intVec.z << 8);
//        ;

        #ifdef MODE_ALPHA
            col = vec4(vec3(intVec) / 255, alpha);
        #else
            col = vec3(intVec) / 255;
        #endif
//        col = vec4(vec3(reconIVal) / 16777215, alpha);
    #else
        #ifdef MODE_ALPHA
            col = vec4(value.xxx, alpha);
        #else
            col = value;
        #endif
    #endif
}
