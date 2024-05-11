package tfc.test.shared;

import tfc.nexirol.render.DataLayout;
import tfc.nexirol.render.ShaderAttachment;
import tfc.nexirol.render.SmartShader;
import tfc.nexirol.render.UniformData;
import tfc.nexirol.render.glsl.*;
import tfc.renirol.frontend.rendering.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.rendering.enums.format.AttributeFormat;
import tfc.renirol.frontend.rendering.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.util.ShaderCompiler;
import tfc.test.Cube;

import java.io.InputStream;

public class Shaders {

    ShaderAttachment[] SKY_ATTACHMENTS;
    ShaderAttachment[] CUBE_ATTACHMENTS;

    public static String read(InputStream is) {
        try {
            byte[] dat = is.readAllBytes();
            try {
                is.close();
            } catch (Throwable ignored) {
            }
            return new String(dat);
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }

    final ShaderCompiler compiler = new ShaderCompiler();

    public static final UniformData matrices = new UniformData(
            new DataFormat(
                    new DataElement(NumericPrimitive.FLOAT, 4 * 4),
                    new DataElement(NumericPrimitive.FLOAT, 4 * 4)
            ),
            new ShaderStageFlags[]{ShaderStageFlags.VERTEX, ShaderStageFlags.FRAGMENT},
            0
    );
    public static final UniformData skyData = new UniformData(
            new DataFormat(
                    // bottom
                    new DataElement(NumericPrimitive.FLOAT, 4),
                    new DataElement(NumericPrimitive.FLOAT, 4),

                    // top
                    new DataElement(NumericPrimitive.FLOAT, 4),
                    new DataElement(NumericPrimitive.FLOAT, 4),

                    // sun
                    new DataElement(NumericPrimitive.FLOAT, 4),
                    new DataElement(NumericPrimitive.FLOAT, 1),
                    new DataElement(NumericPrimitive.FLOAT, 4),

                    // scatter
                    new DataElement(NumericPrimitive.FLOAT, 1),
                    new DataElement(NumericPrimitive.FLOAT, 4),
                    new DataElement(NumericPrimitive.FLOAT, 3),

                    // stars
                    new DataElement(NumericPrimitive.FLOAT, 4 * 4),
                    new DataElement(NumericPrimitive.FLOAT, 1),
                    new DataElement(NumericPrimitive.FLOAT, 1)
            ),
            new ShaderStageFlags[]{ShaderStageFlags.FRAGMENT},
            1
    );

    public static final UniformData cubeInstanceData = new UniformData(
            false, DataLayout.INSTANCE,
            new DataFormat(
                    new DataElement(
                            NumericPrimitive.FLOAT,
                            3 + 4 + 3 + 4,
                            5_000
                    )
            ),
            new ShaderStageFlags[]{ShaderStageFlags.VERTEX},
            1
    );

    public final SmartShader SKY;
    public final SmartShader CUBE;

    public Shaders() {
        matrices.setup(ReniSetup.GRAPHICS_CONTEXT);
        skyData.setup(ReniSetup.GRAPHICS_CONTEXT);
        cubeInstanceData.setup(ReniSetup.GRAPHICS_CONTEXT);

        cubeInstanceData.getDescriptor().attribute(1, 2, AttributeFormat.RGB32_FLOAT, 0);
        cubeInstanceData.getDescriptor().attribute(1, 3, AttributeFormat.RGBA32_FLOAT, 12);
        cubeInstanceData.getDescriptor().attribute(1, 4, AttributeFormat.RGB32_FLOAT, 28);
        cubeInstanceData.getDescriptor().attribute(1, 5, AttributeFormat.RGBA32_FLOAT, 40);

        compiler.setupGlsl();
//        compiler.debug();
        PreProcessor processor = new SequenceProcessor(
                new ImportProcessor(
                        (fl) -> Cube.class.getClassLoader().getResourceAsStream(fl)
                ),
                new MultiProcessor(true, false),
                new ArrowOperatorProcessor()
        );

        SKY = new SmartShader(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                SKY_ATTACHMENTS = new ShaderAttachment[]{
                        new ShaderAttachment(
                                processor, compiler,
                                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                                ShaderStageFlags.VERTEX,
                                read(Shaders.class.getClassLoader().getResourceAsStream("shader/defaults/sky.vsh")),
                                "sky", "main"
                        ),
                        new ShaderAttachment(
                                processor, compiler,
                                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                                ShaderStageFlags.FRAGMENT,
                                read(Shaders.class.getClassLoader().getResourceAsStream("shader/defaults/sky.fsh")),
                                "sky", "main"
                        )
                },
                matrices, skyData
        );
        CUBE = new SmartShader(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                CUBE_ATTACHMENTS = new ShaderAttachment[]{
                        new ShaderAttachment(
                                processor, compiler,
                                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                                ShaderStageFlags.VERTEX,
                                read(Cube.class.getClassLoader().getResourceAsStream("shader/defaults/position_normal.vsh")),
                                "position_normal", "main"
                        ),
                        new ShaderAttachment(
                                processor, compiler,
                                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                                ShaderStageFlags.FRAGMENT,
                                read(Cube.class.getClassLoader().getResourceAsStream("shader/defaults/position_normal.fsh")),
                                "position_normal", "main"
                        )
                },
                matrices, cubeInstanceData
        );
    }

    public void destroy() {
        for (ShaderAttachment skyAttachment : SKY_ATTACHMENTS) skyAttachment.destroy();
        for (ShaderAttachment skyAttachment : CUBE_ATTACHMENTS) skyAttachment.destroy();
        SKY.destroy();
        CUBE.destroy();
    }
}
