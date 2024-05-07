//@formatter:off
#version 450
#extension GL_ARB_separate_shader_objects : enable
//#extension GL_EXT_gpu_shader4 : enable

layout(location = 0) in vec4 fgCoord;

layout(location = 0) out vec4 colorOut;

layout(binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};

layout(binding = 0) uniform Info {
    vec4 BottomColor1;
    vec4 BottomColor0;

    vec4 TopColor0;
    vec4 TopColor1;

    vec4 SunDir;
    float SunSize;
    vec4 SunColor;

    float Scattering;
    vec4 ScatterColor;
    vec3 ScatterDir;

    mat4 StarRotation;
    float StarDensity;
    float StarVisibility;
};

#include <shader/util/quat_math.glsl>

// 0.25
#define SKYBOX_BOTTOM 0.15

void main() {
//    vec4 coord = normalize(rotate(projectionMatrix * normalize(fgCoord), rotation));
//    colorOut = vec4(normalize(coord.xyz), 1);
//    if (colorOut.y < coord.y) colorOut = vec4(0);
//    colorOut = abs(colorOut);
    // TODO: figure out how to do a sky quad

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
            vec2 sunOpposite2D = normalize(vec2(sunCoord.xz));
            vec3 scatterOpposite = normalize(vec3(sunOpposite2D.x * 0.5, -0.8, sunOpposite2D.y * 0.5));

            // calculate scatter amount
            float col = dot(scatterOpposite, coord.xyz);
            float cscale = 1.0;
            if (sunCoord.y < 1.0)
                cscale *= 1.0 - abs(sunCoord.y);
            if (coord.y < 0.0) {
				float c = coord.y;
				c /= 4.0;
                cscale *= 1.0 - c;
            }
            col = clamp(col * cscale, 0, 1);

            // apply scatter color
            colorOut = mix(
                colorOut,
                ScatterColor * 3.0,
                col
            );
            // create a ring around the horizon
            // TODO: see about fading this as the pixel gets further from the sun?
            vec3 sun90 = normalize(rotate(vec4(0, 1, 0, 1), SunDir).xyz);
            float ccord = abs((dot(coord.xyz, normalize(sun90 + vec3(0, 3 * sign(sun90.y), 0))) + 0.05) * 5);
            ccord = clamp(sqrt(abs(ccord)), 0.0, 1.0);

            colorOut = mix(
                colorOut,
                ScatterColor * 3.0,
                (1 - ccord) * (cscale * cscale)
            );

            // makes colors far away from the sun darker
            float dSun2 = distance(normalize(coord.xyz), normalize(sunCoord.xyz + vec3(0, 1, 0))) / SunSize;
            float mul = (dSun2 / 50);
            if (sunCoord.y > 0)
                mul /= (10 * sunCoord.y * sunCoord.y) + 1;
            if (mul > 0.8) {
                mul /= 2.0;
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
            float c = coord.y + SKYBOX_BOTTOM * 4.;
            c /= 4.0;
            if (sunCoord.y > 0.) c = mix(c, 1, sunCoord.y * 2.);
            if (c > 1.) c = 1.;
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
            dSun = clamp(dSun, 0.75f, 100);
            dSun -= 0.5;
            dSun = clamp(dSun, 0, 1);
            colorOut = mix(
                SunColor,
                colorOut,
                dSun
            );
        }
    }
}
