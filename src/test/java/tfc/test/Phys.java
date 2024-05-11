package tfc.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import tfc.nexirol.math.Matrices;
import tfc.nexirol.physics.bullet.BulletWorld;
import tfc.nexirol.physics.wrapper.Material;
import tfc.nexirol.physics.wrapper.PhysicsDrawable;
import tfc.nexirol.physics.wrapper.PhysicsWorld;
import tfc.nexirol.physics.wrapper.RigidBody;
import tfc.nexirol.physics.wrapper.shape.Cube;
import tfc.nexirol.primitives.CubePrimitive;
import tfc.nexirol.render.UniformData;
import tfc.renirol.frontend.hardware.device.ReniQueueType;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.enums.ImageLayout;
import tfc.renirol.frontend.rendering.enums.Operation;
import tfc.renirol.frontend.rendering.enums.format.AttributeFormat;
import tfc.renirol.frontend.rendering.enums.masks.DynamicStateMasks;
import tfc.renirol.frontend.rendering.enums.modes.CullMode;
import tfc.renirol.frontend.rendering.pass.RenderPass;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.reni.draw.instance.InstanceCollection;
import tfc.renirol.frontend.windowing.glfw.GLFWWindow;
import tfc.test.data.VertexElements;
import tfc.test.data.VertexFormats;
import tfc.test.shared.ReniSetup;
import tfc.test.shared.Shaders;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Phys {
    public static void main(String[] args) {
        System.setProperty("joml.nounsafe", "true");
        ReniSetup.initialize();

        Shaders shaders = new Shaders();

        final RenderPass pass;
        final RenderPass pass1;
        {
            RenderPassInfo info = new RenderPassInfo(ReniSetup.GRAPHICS_CONTEXT.getLogical(), ReniSetup.GRAPHICS_CONTEXT.getSurface());
            pass = info.colorAttachment(
                    Operation.CLEAR, Operation.PERFORM,
                    ImageLayout.UNDEFINED, ImageLayout.PRESENT,
                    ReniSetup.selector
            ).depthAttachment(
                    Operation.CLEAR, Operation.PERFORM,
                    ImageLayout.UNDEFINED, ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    ReniSetup.DEPTH_FORMAT
            ).dependency().subpass().create();
            info.destroy();

            info = new RenderPassInfo(ReniSetup.GRAPHICS_CONTEXT.getLogical(), ReniSetup.GRAPHICS_CONTEXT.getSurface());
            pass1 = info.colorAttachment(
                    Operation.PERFORM, Operation.PERFORM,
                    ImageLayout.UNDEFINED, ImageLayout.PRESENT,
                    ReniSetup.selector
            ).depthAttachment(
                    Operation.PERFORM, Operation.PERFORM,
                    ImageLayout.UNDEFINED, ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    ReniSetup.DEPTH_FORMAT
            ).dependency().subpass().create();
            info.destroy();
        }

        PipelineState state = new PipelineState(ReniSetup.GRAPHICS_CONTEXT.getLogical());
        state.dynamicState(DynamicStateMasks.SCISSOR, DynamicStateMasks.VIEWPORT, DynamicStateMasks.CULL_MODE);

        DataFormat format = VertexFormats.POS4_NORMAL3;

        final BufferDescriptor desc0 = new BufferDescriptor(format);
        desc0.describe(0);
        desc0.attribute(0, 0, AttributeFormat.RGBA32_FLOAT, 0);
        desc0.attribute(0, 1, AttributeFormat.RGB32_FLOAT, format.offset(VertexElements.NORMAL_XYZ));

        state.vertexInput(desc0);

        shaders.SKY.prepare();
        shaders.SKY.bind(state, desc0);
        state.depthTest(false).depthMask(false);
        GraphicsPipeline pipeline0 = new GraphicsPipeline(state, pass, shaders.SKY.shaders);
        shaders.CUBE.prepare();
        shaders.CUBE.bind(state, desc0);
        state.depthTest(true).depthMask(true);
        GraphicsPipeline pipeline1 = new GraphicsPipeline(state, pass, shaders.CUBE.shaders);

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

//            PhysicsWorld world = new BulletWorld();
            PhysicsWorld world = new BulletWorld();

            final int MAX_INSTANCES = 5_000;
            final int SHADER_MAX_INSTANCES = 5_000;
            InstanceCollection collection = new InstanceCollection(
                    (collection1) -> {
                        collection1.maxInstances = SHADER_MAX_INSTANCES;
                        collection1.pipeline = pipeline1;
                    }
            );

            long[] frameHandle = new long[1];
            VkExtent2D[] extent2D = new VkExtent2D[1];

            // ground
            {
                float r = (float) Math.random();
                float g = (float) Math.random();
                float b = (float) Math.random();
                RigidBody body;
                world.addBody(body = new RigidBody(
                        true, new Cube(100, 1, 100),
                        new Material(1, 1, 1) // probably gonna have some stupid result, lol
                ).setPosition(
                        0, -40, 0
                ));
                collection.add(buffer, new PhysicsDrawable(
                        cube,
                        (cmd, idx) -> {
                            ByteBuffer cubeBuf = Shaders.cubeInstanceData.get(idx);
                            FloatBuffer fb = cubeBuf.asFloatBuffer();
                            // position
                            fb.put(0, body.vec.x);
                            fb.put(1, body.vec.y);
                            fb.put(2, body.vec.z);
                            // orientation
                            fb.put(3, body.quat.x);
                            fb.put(4, body.quat.y);
                            fb.put(5, body.quat.z);
                            fb.put(6, body.quat.w);
                            // scale
                            fb.put(7, 100);
                            fb.put(8, 1);
                            fb.put(9, 100);
                            // color
                            fb.put(10, r);
                            fb.put(11, g);
                            fb.put(12, b);
                            fb.put(13, 1);
                        },
                        (cmd) -> {
                            if (ReniSetup.NVIDIA) {
                                Shaders.cubeInstanceData.upload(cmd);
                            } else {
                                cmd.endPass();
                                Shaders.cubeInstanceData.upload(cmd);
                                cmd.beginPass(pass1, frameHandle[0], extent2D[0]);
                            }
                        },
                        body
                ));
            }
            // physics objects
            for (int i = 0; i < (MAX_INSTANCES - 1); i++) {
                float size = (float) (Math.random() * 2 + 1);
                float r = (float) Math.random();
                float g = (float) Math.random();
                float b = (float) Math.random();
                RigidBody body;
                world.addBody(body = new RigidBody(
                        false, new Cube(size, size, size),
                        new Material(1, 1, 1) // probably gonna have some stupid result, lol
                ).setPosition(
                        (float) (Math.random() * 25f - (25f / 2)),
                        (float) (Math.random() * 25f - (25f / 2)) + i,
                        (float) (Math.random() * 25f - (25f / 2))
                ));

                collection.add(buffer, new PhysicsDrawable(
                        cube,
                        (cmd, idx) -> {
                            ByteBuffer cubeBuf = Shaders.cubeInstanceData.get(idx);
                            FloatBuffer fb = cubeBuf.asFloatBuffer();
                            // position
                            fb.put(0, body.vec.x);
                            fb.put(1, body.vec.y);
                            fb.put(2, body.vec.z);
                            // orientation
                            fb.put(3, body.quat.x);
                            fb.put(4, body.quat.y);
                            fb.put(5, body.quat.z);
                            fb.put(6, body.quat.w);
                            // scale
                            fb.put(7, size);
                            fb.put(8, size);
                            fb.put(9, size);
                            // color
                            fb.put(10, r);
                            fb.put(11, g);
                            fb.put(12, b);
                            fb.put(13, 1);
                        },
//                            Shaders.cubeInstanceData::upload
                        (cmd) -> {
                            if (ReniSetup.NVIDIA) {
                                Shaders.cubeInstanceData.upload(cmd);
                            } else {
                                cmd.endPass();
                                Shaders.cubeInstanceData.upload(cmd);
                                cmd.beginPass(pass1, frameHandle[0], extent2D[0]);
                            }
                        },
                        body
                ));
            }

            while (!ReniSetup.WINDOW.shouldClose()) {
                frame++;

                {
                    UniformData matrices = Shaders.matrices;

                    matrices.set(0, Matrices.projection((float) Math.toRadians(45), ReniSetup.WINDOW.getWidth(), ReniSetup.WINDOW.getHeight(), 0.01f, 300.0f));

                    Matrix4f view = new Matrix4f();
                    float x = (float) Math.cos(Math.toRadians(frame / 10.));
                    float y = (float) Math.sin(Math.toRadians(frame / 10.));
                    view.setLookAt(x * 100f, 20f, y * 100f,
                            0.0f, -30.0f, 0.0f,
                            0.0f, -1.0f, 0.0f);
                    Matrix4f model = new Matrix4f();

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
                frameHandle[0] = fbo;
                extent2D[0] = ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents();

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
                    world.tick();
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
