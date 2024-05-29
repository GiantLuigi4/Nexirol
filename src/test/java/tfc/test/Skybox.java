package tfc.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import tfc.nexirol.math.Matrices;
import tfc.nexirol.primitives.CubePrimitive;
import tfc.nexirol.render.ShaderAttachment;
import tfc.nexirol.render.SmartShader;
import tfc.nexirol.render.UniformData;
import tfc.nexirol.render.glsl.ImportProcessor;
import tfc.renirol.frontend.enums.DescriptorType;
import tfc.renirol.frontend.enums.ImageLayout;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.flags.DescriptorPoolFlags;
import tfc.renirol.frontend.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.enums.format.AttributeFormat;
import tfc.renirol.frontend.enums.masks.DynamicStateMasks;
import tfc.renirol.frontend.enums.modes.CullMode;
import tfc.renirol.frontend.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.hardware.device.ReniQueueType;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorPool;
import tfc.renirol.frontend.windowing.glfw.GLFWWindow;
import tfc.renirol.util.ShaderCompiler;
import tfc.test.data.VertexFormats;
import tfc.test.shared.ReniSetup;

import java.io.InputStream;

public class Skybox {
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

    public static void main(String[] args) {
        System.setProperty("joml.nounsafe", "true");
        ReniSetup.initialize();
        matrices.setup(ReniSetup.GRAPHICS_CONTEXT);
        skyData.setup(ReniSetup.GRAPHICS_CONTEXT);

        final RenderPassInfo pass;
        {
            pass = new RenderPassInfo(ReniSetup.GRAPHICS_CONTEXT.getLogical(), ReniSetup.GRAPHICS_CONTEXT.getSurface());
            pass.colorAttachment(
                    Operation.CLEAR, Operation.NONE,
                    ImageLayout.UNDEFINED, ImageLayout.PRESENT,
                    ReniSetup.selector
            ).depthAttachment(
                    Operation.CLEAR, Operation.NONE,
                    ImageLayout.UNDEFINED, ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    ReniSetup.DEPTH_FORMAT
            );
        }

        final ShaderCompiler compiler = new ShaderCompiler();
        compiler.setupGlsl();
        compiler.debug();
        ImportProcessor processor = new ImportProcessor(
                (fl) -> Skybox.class.getClassLoader().getResourceAsStream(fl)
        );
        ShaderAttachment[] SKY_ATTACHMENTS;
        SmartShader SKY = new SmartShader(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                SKY_ATTACHMENTS = new ShaderAttachment[]{
                        new ShaderAttachment(
                                processor, compiler,
                                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                                ShaderStageFlags.VERTEX,
                                read(Skybox.class.getClassLoader().getResourceAsStream("shader/defaults/sky.vsh")),
                                "sky", "main"
                        ),
                        new ShaderAttachment(
                                processor, compiler,
                                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                                ShaderStageFlags.FRAGMENT,
                                read(Skybox.class.getClassLoader().getResourceAsStream("shader/defaults/sky.fsh")),
                                "sky", "main"
                        )
                },
                matrices, skyData
        );

        PipelineState state = new PipelineState(ReniSetup.GRAPHICS_CONTEXT.getLogical());
        state.depthTest(true).depthMask(false);
        state.dynamicState(DynamicStateMasks.SCISSOR, DynamicStateMasks.VIEWPORT);

        DataFormat format = VertexFormats.POS4_NORMAL3;

        final BufferDescriptor desc0 = new BufferDescriptor(format);
        desc0.describe(0);
        desc0.attribute(0, 0, AttributeFormat.RGB32_FLOAT, 0);

        // TODO: ideally this stuff would be abstracted away more
        final DescriptorPool pool = new DescriptorPool(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                1,
                new DescriptorPoolFlags[0],
                DescriptorPool.PoolInfo.of(DescriptorType.UNIFORM_BUFFER, 10)
        );
//        DescriptorLayout layout;
//        {
//            final DescriptorLayoutInfo info = new DescriptorLayoutInfo(
//                    0, DescriptorType.COMBINED_SAMPLED_IMAGE,
//                    1, ShaderStageFlags.FRAGMENT
//            );
//
//            layout = new DescriptorLayout(
//                    tfc.test.shared.ReniSetup.GRAPHICS_CONTEXT.getLogical(),
//                    0, info
//            );
//
//            info.destroy();
//        }
//        DescriptorSet set = new DescriptorSet(
//                tfc.test.shared.ReniSetup.GRAPHICS_CONTEXT.getLogical(),
//                pool, layout
//        );
//        state.descriptorLayouts(layout);

//        InputStream is = PrimaryTest.class.getClassLoader().getResourceAsStream("test/texture/texture.png");
//        Texture texture = new Texture(
//                tfc.test.shared.ReniSetup.GRAPHICS_CONTEXT.getLogical(),
//                TextureFormat.PNG, TextureChannels.RGBA,
//                BitDepth.DEPTH_8, is
//        );
//        TextureSampler sampler = texture.createSampler(
//                WrapMode.BORDER,
//                WrapMode.BORDER,
//                FilterMode.NEAREST,
//                FilterMode.NEAREST,
//                MipmapMode.NEAREST,
//                true, 16f,
//                0f, 0f, 0f
//        );
//        ImageInfo info = new ImageInfo(texture, sampler);
//        set.bind(0, 0, DescriptorType.COMBINED_SAMPLED_IMAGE, info);
//        try {
//            is.close();
//        } catch (Throwable ignored) {
//        }

        SKY.prepare();
        SKY.bind(state, desc0);
        GraphicsPipeline pipeline0 = new GraphicsPipeline(pass, state, SKY.shaders);

        CubePrimitive cube = new CubePrimitive(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                format, 1, 1, 1
        );

        try {
            int frame = 0;

            ReniSetup.WINDOW.grabContext();
            final CommandBuffer buffer = CommandBuffer.create(
                    ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                    ReniQueueType.GRAPHICS, true,
                    false
            );
            buffer.clearColor(0, 0, 0, 1);
            buffer.clearDepth(1f);

            while (!ReniSetup.WINDOW.shouldClose()) {
                frame++;

                {
                    matrices.set(0, Matrices.projection((float) Math.toRadians(45), ReniSetup.WINDOW.getWidth(), ReniSetup.WINDOW.getHeight(), 0.01f, 100.0f));

                    Matrix4f view = new Matrix4f();
                    view.setLookAt(0.0f, 0.0f, -50.0f,
                            0.0f, 0.0f, 0.0f,
                            0.0f, -1.0f, 0.0f);
                    Matrix4f model = new Matrix4f();
                    model.translate(15, -20, 0);
                    model.rotate(new Quaternionf().fromAxisAngleDeg(1, 0, 0, 22.5f));
//                    model.rotate(new Quaternionf().fromAxisAngleDeg(1, 0, 0, -frame));

                    model.mul(view);
                    matrices.set(1, model);
                    matrices.upload();
                }
                // setup sky
                {
                    // skybox
                    skyData.setColor(0, (int) ((201 / 325f) * 255f), (int) ((226 / 325f) * 255f), (int) ((254 / 325f) * 255f), 255);
                    skyData.setColor(1, 201, 226, 254, 255);
                    skyData.setColor(2, 201, 226, 254, 0);
                    skyData.setColor(3, 81, 133, 201, 0);

                    // sun
                    skyData.set(4, new Quaternionf().setAngleAxis(
                            Math.toRadians(frame / 10f), 1, 0, 0
                    ));
                    skyData.set(5, 1 / 32f);
                    skyData.set(6, 1, 1, 1, 1f);

                    // scatter
                    skyData.set(7, 0);
                    skyData.set(8, 168 / 255f, 113 / 255f, 50 / 255f, 1);
                    skyData.set(9, 0, 0, 0);

                    // TODO: stars

                    skyData.upload();
                }

                ReniSetup.GRAPHICS_CONTEXT.prepareFrame(ReniSetup.WINDOW);

                buffer.begin();

                buffer.startLabel("Main Pass", 0.5f, 0, 0, 0.5f);
                buffer.beginPass(pass, ReniSetup.GRAPHICS_CONTEXT.getChainBuffer(), ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents());

                buffer.bindPipe(pipeline0);
//                buffer.bindDescriptor(BindPoint.GRAPHICS, pipeline0, set);
                SKY.bindCommand(pipeline0, buffer);
                buffer.cullMode(CullMode.BACK);
                buffer.viewportScissor(
                        0, 0,
                        ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().width(),
                        ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().height(),
                        0f, 1f
                );
                cube.draw(buffer);
                buffer.endPass();
                buffer.endLabel();
                // TODO: setup access flags
//                buffer.transition(
//                        tfc.test.shared.ReniSetup.GRAPHICS_CONTEXT.getFramebuffer().image,
//                        StageMask.TOP_OF_PIPE,
//                        StageMask.TRANSFER,
//                        ImageLayout.PRESENT,
//                        ImageLayout.PRESENT
//                );
                buffer.end();

                ReniSetup.GRAPHICS_CONTEXT.submitFrame(buffer);
                ReniSetup.GRAPHICS_CONTEXT.getLogical().getStandardQueue(ReniQueueType.GRAPHICS).await();

                ReniSetup.WINDOW.swapAndPollSize();
                GLFWWindow.poll();

                ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();
            }
            buffer.destroy();
        } catch (Throwable err) {
            err.printStackTrace();
        }

//        info.destroy();
//        sampler.destroy();
//        texture.destroy();
//        layout.destroy();
        pool.destroy();
        cube.destroy();
        ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();
        for (ShaderAttachment skyAttachment : SKY_ATTACHMENTS) skyAttachment.destroy();
        SKY.destroy();
        desc0.destroy();
        pipeline0.destroy();
        pass.destroy();
        ReniSetup.GRAPHICS_CONTEXT.destroy();
        ReniSetup.WINDOW.dispose();
        GLFW.glfwTerminate();
    }

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
}
