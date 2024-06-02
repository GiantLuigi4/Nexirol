//@formatter:off
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec4 fgCoord;

layout(location = 0) out vec4 colorOut;

layout(binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};

layout(binding = 1) uniform Info {
    vec4 BottomColor1;    // 0
    vec4 BottomColor0;    // 1

    vec4 TopColor0;       // 2
    vec4 TopColor1;       // 3

    vec4 SunDir;          // 4
    float SunSize;        // 5
    vec4 SunColor;        // 6

    float Scattering;     // 7
    vec4 ScatterColor;    // 8
    vec3 ScatterDir;      // 9

    mat4 StarRotation;    // 10
    float StarDensity;    // 11
    float StarVisibility; // 12
};

#include <shader/util/math/quats.glsl>

// 0.25
#define SKYBOX_BOTTOM 0.15

// TODO: refactor
void main() {
    vec4 coord = vec4(normalize(fgCoord.xyz), 1);

    colorOut = TopColor0;

    // TODO: stars
    // TODO: moon

    /* base sky */
    if (coord.y > 0) {
        colorOut = mix(TopColor0, TopColor1, coord.y);
    } else if (coord.y < -0.) {
        vec4 cc = coord;
        if (cc.y < -SKYBOX_BOTTOM) cc.y = -SKYBOX_BOTTOM;
        cc.y *= (1 / SKYBOX_BOTTOM);
        cc = cc + 1;

        vec4 lowerColor = mix(BottomColor0, BottomColor1, -coord.y);
        colorOut = mix(lowerColor, colorOut, cc.y);
    }

    if (SunSize != 0) {
        vec3 sunCoord = normalize(rotate(vec4(0, 0, 1, 1), SunDir).xyz);
        float dSun = distance(normalize(coord.xyz), sunCoord.xyz) / SunSize;

        /* scattering */ {
            float cMul = 1-abs(sunCoord.y);
            cMul *= cMul;

            vec2 sunOpposite2D = normalize(vec2(sunCoord.xz));
            vec3 scatterOpposite = normalize(vec3(sunOpposite2D.x * 0.5, -1.2, sunOpposite2D.y * 0.5));

            // calculate scatter amount
            float col = dot(scatterOpposite, coord.xyz);

            float cscale = 1.0;
            if (sunCoord.y < 1.0)
                cscale *= 1.0 - abs(sunCoord.y);
            if (coord.y < 0.0)
                cscale *= 1.0 - (coord.y * 0.25);

            col = clamp(col * cscale * 2., 0, 1);

            // apply scatter color
            colorOut = mix(
                colorOut,
                mix(
                    ScatterColor,
                    ScatterColor.brga * vec4(0.0, 1, 0.0, 1),
                    clamp((1-col) - 0.75, 0.0, 0.25) * 1.5
                ) * 3.0,
                col * cMul
            );

            // more scattering
            vec3 sun90 = normalize(rotate(vec4(0, 1, 0, 1), SunDir).xyz);
            float u = sign(sun90.y);
            float ccord = dot(coord.xyz, normalize(sun90 + vec3(0, 3.0 * u, 0))) * 5.25 + 0.5 * u;
            float v = sign(ccord) * u;
            ccord = clamp(ccord * ccord, 0.0, 1.0);
            ccord *= step(0.0, v);

            colorOut = mix(
                colorOut,
                mix(
                    ScatterColor,
                    ScatterColor.brga * vec4(0.0, 1, 0.0, 1),
                    clamp((1 - ((1 - ccord) * (cscale * cscale))) - 0.75, 0.0, 0.25) * 1.5
                ) * 3.0,
                (1 - ccord) * (cscale * cscale) * abs(u) * cMul
            );

            // makes colors far away from the sun darker
            float dSun2 = distance(normalize(coord.xyz), normalize(sunCoord.xyz + vec3(0, 1, 0))) / SunSize;
            float mul = (dSun2 * 0.02);
            if (sunCoord.y > 0)
                mul /= (10 * sunCoord.y * sunCoord.y) + 1;
            if (mul > 0.8) {
                mul *= 0.5;
                mul += 0.4;
            }
            mul *= mul;
            colorOut = mix(
                vec4(0),
                colorOut,
                1 - mul
            );

            // fades out lower part of skybox as sun sets
            // makes it look less janky when the sun is low
            float c = (coord.y + SKYBOX_BOTTOM * 4.0) * 0.25;
            if (sunCoord.y > 0.) c = mix(c, 1, sunCoord.y * 2.);
            c = min(c, 1);
            colorOut *= c;

            // fades the skybox out as the sun sets
            if (sunCoord.y < 0) {
                colorOut *= clamp(
                    (1 - abs(sunCoord.y * 2)),
                    0, 1
                );
            }
        }

        /* sun */ {
            // TODO: sun disc options
            float disc = dSun;

            dSun = clamp((dSun - 0.5) * 1.5, 0.0, 1.0);

            disc = clamp((disc - 0.5) * 0.025 / clamp(sunCoord.y, 0.05, 1.0), 0.0, 1.0);
            disc = clamp((1.0 - sqrt(disc)) / 1.75, 0.0, 1.0);

            colorOut = mix(
                    colorOut,
                    (vec4((disc + 0.25).xxx, 1) * (clamp(sunCoord.y, 0.0, 0.5) + 1.0) * SunColor),
                    disc
            );
            disc *= 0.25;
            colorOut += vec4(disc.xxx, 1) * clamp(sunCoord.y, 0, 1) * SunColor;

            colorOut = mix(
                SunColor,
                colorOut,
                dSun
            );
        }
    }
}
