package tfc.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDevice;
import tfc.nexirol.math.Matrices;
import tfc.nexirol.primitives.CubePrimitive;
import tfc.nexirol.render.UniformData;
import tfc.renirol.frontend.enums.DescriptorType;
import tfc.renirol.frontend.enums.ImageLayout;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.flags.DescriptorPoolFlags;
import tfc.renirol.frontend.enums.format.AttributeFormat;
import tfc.renirol.frontend.enums.masks.DynamicStateMasks;
import tfc.renirol.frontend.enums.modes.CullMode;
import tfc.renirol.frontend.hardware.device.ReniQueueType;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.pass.RenderPass;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorPool;
import tfc.renirol.frontend.reni.draw.instance.InstanceCollection;
import tfc.renirol.frontend.windowing.glfw.GLFWWindow;
import tfc.test.data.VertexElements;
import tfc.test.data.VertexFormats;
import tfc.test.shared.ReniSetup;
import tfc.test.shared.Shaders;

public class Cube {
    public static void main(String[] args) {
        System.setProperty("joml.nounsafe", "true");
        ReniSetup.initialize();

        Shaders shaders = new Shaders();

        final RenderPass pass;
        {
            RenderPassInfo info = new RenderPassInfo(ReniSetup.GRAPHICS_CONTEXT.getLogical(), ReniSetup.GRAPHICS_CONTEXT.getSurface());
            pass = info.colorAttachment(
                    Operation.CLEAR, Operation.NONE,
                    ImageLayout.UNDEFINED, ImageLayout.PRESENT,
                    ReniSetup.selector
            ).depthAttachment(
                    Operation.CLEAR, Operation.NONE,
                    ImageLayout.UNDEFINED, ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    ReniSetup.DEPTH_FORMAT
            ).dependency().subpass().create();
            info.destroy();
        }

        PipelineState state = new PipelineState(ReniSetup.GRAPHICS_CONTEXT.getLogical());
        state.depthTest(false).depthMask(false);
        state.dynamicState(DynamicStateMasks.SCISSOR, DynamicStateMasks.VIEWPORT, DynamicStateMasks.CULL_MODE);

        DataFormat format = VertexFormats.POS4_NORMAL3;

        final BufferDescriptor desc0 = new BufferDescriptor(format);
        desc0.describe(0);
        desc0.attribute(0, 0, AttributeFormat.RGBA32_FLOAT, 0);
        desc0.attribute(0, 1, AttributeFormat.RGB32_FLOAT, format.offset(VertexElements.NORMAL_XYZ));

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

        shaders.SKY.prepare();
        shaders.SKY.bind(state, desc0);
        GraphicsPipeline pipeline0 = new GraphicsPipeline(state, pass, shaders.SKY.shaders);
        shaders.CUBE.prepare();
        shaders.CUBE.bind(state, desc0);
        state.depthTest(true).depthMask(true);
        GraphicsPipeline pipeline1 = new GraphicsPipeline(state, pass, shaders.CUBE.shaders);

        CubePrimitive cube = new CubePrimitive(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                format, 1, 1, 1
        );
        CubePrimitive cube1 = new CubePrimitive(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                format, 1.5f, 0.5f, 0.5f
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

            for (int i = 0; i < 100; i++) {
                // position
                Shaders.cubeInstanceData.set(
                        i,
                        (float) Math.random() * 100 - 50,
                        (float) Math.random() * 100 - 50,
                        (float) Math.random() * 100 - 50
                );
                // orientation
                Shaders.cubeInstanceData.set(
                        i + 100, 0, 0, 0, 1
                );
                // scale
                Shaders.cubeInstanceData.set(
                        i + 200,
                        1, 1, 1
                );
                // color
                Shaders.cubeInstanceData.set(
                        i + 300, (float) Math.random(), (float) Math.random(), (float) Math.random(), 1
                );
            }
            Shaders.cubeInstanceData.upload();

            InstanceCollection collection = new InstanceCollection(
                    (collection1) -> {
                        collection1.maxInstances = 100;
                        collection1.pipeline = pipeline1;
                    }
            );
            for (int i = 0; i < 100; i++) {
                collection.add(buffer, cube);
            }
            collection.add(buffer, cube1);

            while (!ReniSetup.WINDOW.shouldClose()) {
                frame++;

                {
                    UniformData matrices = Shaders.matrices;

                    matrices.set(0, Matrices.projection((float) Math.toRadians(45), ReniSetup.WINDOW.getWidth(), ReniSetup.WINDOW.getHeight(), 0.01f, 100.0f));

                    Matrix4f view = new Matrix4f();
                    float x = (float) Math.cos(Math.toRadians(frame / 10.));
                    float y = (float) Math.sin(Math.toRadians(frame / 10.));
                    view.setLookAt(x * 10f, 5f, y * 10f,
                            0.0f, 0.0f, 0.0f,
                            0.0f, -1.0f, 0.0f);
                    Matrix4f model = new Matrix4f();
//                    model.translate(0, 0, 50);
//                    model.translate(0, -20, 0);
//                    model.rotate(new Quaternionf().fromAxisAngleDeg(1, 0, 0, 22.5f));
//                    model.rotate(new Quaternionf().fromAxisAngleDeg(0, 1, 0, -frame));
//                    model.translate(0, 0, -50);

                    model.mul(view);
                    matrices.set(1, model);
                    matrices.upload();
                }
                // setup sky
                {
                    UniformData skyData = Shaders.skyData;

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
                long fbo = ReniSetup.GRAPHICS_CONTEXT.getFrameHandle(pass);

                buffer.begin();

                buffer.startLabel("Main Pass", 0.5f, 0, 0, 0.5f);
                buffer.beginPass(pass, fbo, ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents());
                {
                    buffer.startLabel("Sky", 0, 0.5f, 0, 0.5f);

                    buffer.bindPipe(pipeline0);
                    shaders.SKY.bindCommand(pipeline0, buffer);
                    buffer.cullMode(CullMode.FRONT);
                    buffer.viewportScissor(
                            0, 0,
                            ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().width(),
                            ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().height(),
                            0f, 1f
                    );
                    cube.bind(buffer);
                    cube.draw(buffer);
                    buffer.endLabel();
                }
                {
                    buffer.startLabel("Scene", 0, 0, 0.5f, 0.5f);

                    buffer.bindPipe(pipeline1);
                    shaders.CUBE.bindCommand(pipeline1, buffer);
                    buffer.cullMode(CullMode.BACK);
                    buffer.viewportScissor(
                            0, 0,
                            ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().width(),
                            ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().height(),
                            0f, 1f
                    );
                    collection.draw(buffer, pipeline1);
                    buffer.endLabel();
                }
                buffer.endPass();
                buffer.endLabel();
                buffer.end();

                ReniSetup.GRAPHICS_CONTEXT.submitFrame(buffer);
                ReniSetup.GRAPHICS_CONTEXT.getLogical().getStandardQueue(ReniQueueType.GRAPHICS).await();

                ReniSetup.WINDOW.swapAndPollSize();
                GLFWWindow.poll();

                ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();

                VK13.nvkDestroyFramebuffer(ReniSetup.GRAPHICS_CONTEXT.getLogical().getDirect(VkDevice.class), fbo, 0);
            }
            buffer.destroy();
        } catch (Throwable err) {
            err.printStackTrace();
        }

        pool.destroy();
        cube.destroy();
        ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();
        shaders.destroy();
        desc0.destroy();
        pipeline0.destroy();
        pass.destroy();
        ReniSetup.GRAPHICS_CONTEXT.destroy();
        ReniSetup.WINDOW.dispose();
        GLFW.glfwTerminate();
    }
}
