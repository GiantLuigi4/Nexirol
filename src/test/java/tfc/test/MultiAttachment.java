package tfc.test;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkExtent2D;
import tfc.nexirol.math.Matrices;
import tfc.nexirol.physics.physx.PhysXWorld;
import tfc.nexirol.physics.wrapper.Material;
import tfc.nexirol.physics.wrapper.PhysicsDrawable;
import tfc.nexirol.physics.wrapper.PhysicsWorld;
import tfc.nexirol.physics.wrapper.RigidBody;
import tfc.nexirol.physics.wrapper.shape.Cube;
import tfc.nexirol.primitives.CubePrimitive;
import tfc.nexirol.render.UniformData;
import tfc.renirol.frontend.enums.DescriptorType;
import tfc.renirol.frontend.enums.ImageLayout;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.flags.DescriptorPoolFlags;
import tfc.renirol.frontend.enums.flags.SwapchainUsage;
import tfc.renirol.frontend.enums.format.AttributeFormat;
import tfc.renirol.frontend.enums.masks.DynamicStateMasks;
import tfc.renirol.frontend.enums.modes.CullMode;
import tfc.renirol.frontend.hardware.device.ReniQueueType;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorPool;
import tfc.renirol.frontend.rendering.resource.image.Image;
import tfc.renirol.frontend.reni.draw.instance.InstanceCollection;
import tfc.renirol.frontend.windowing.glfw.GLFWWindow;
import tfc.renirol.frontend.windowing.listener.KeyboardListener;
import tfc.test.data.VertexElements;
import tfc.test.data.VertexFormats;
import tfc.test.shared.ReniSetup;
import tfc.test.shared.Scenario;
import tfc.test.shared.Shaders;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class MultiAttachment {
    public static void main(String[] args) {
//        System.setProperty("joml.nounsafe", "true");
        Scenario.useWinNT = false;
//        Scenario.useRenderDoc = false;
        ReniSetup.initialize();
        Image col;
        ReniSetup.GRAPHICS_CONTEXT.addBuffer(
                col = new Image(ReniSetup.GRAPHICS_CONTEXT.getLogical())
                        .setUsage(SwapchainUsage.COLOR),
                false
        );
        col.create(
                ReniSetup.WINDOW.getWidth(),
                ReniSetup.WINDOW.getHeight(),
                VK13.VK_FORMAT_R8G8B8A8_SRGB
        );

        Shaders shaders = new Shaders();

        final RenderPassInfo pass = ReniSetup.GRAPHICS_CONTEXT.getPass(
                Operation.CLEAR, Operation.PERFORM,
                ImageLayout.COLOR_ATTACHMENT_OPTIMAL
        );
        final RenderPassInfo pass1 = ReniSetup.GRAPHICS_CONTEXT.getPass(
                Operation.PERFORM, Operation.PERFORM,
                ImageLayout.COLOR_ATTACHMENT_OPTIMAL
        );

        PipelineState state = new PipelineState(ReniSetup.GRAPHICS_CONTEXT.getLogical());
        state.colorAttachmentCount(2);
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

        shaders.SKY.prepare();
        shaders.SKY.bind(state, desc0);
        state.depthTest(false).depthMask(false);
        GraphicsPipeline pipeline0 = new GraphicsPipeline(pass, state, shaders.SKY.shaders);
        shaders.CUBE.prepare();
        shaders.CUBE.bind(state, desc0);
        state.depthTest(true).depthMask(true);
        GraphicsPipeline pipeline1 = new GraphicsPipeline(pass, state, shaders.CUBE.shaders);

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
//            PhysicsWorld world = new BulletWorld();
            PhysicsWorld world = new PhysXWorld();

            final int MAX_INSTANCES = 50_000;
            final int SHADER_MAX_INSTANCES = 1_000;
            InstanceCollection collection = new InstanceCollection(
                    (collection1) -> {
                        collection1.maxInstances = SHADER_MAX_INSTANCES;
                        collection1.pipeline = pipeline1;
                    }
            );

            VkExtent2D[] extent2D = new VkExtent2D[1];

            // ground
            {
                float r = (float) Math.random();
                float g = (float) Math.random();
                float b = (float) Math.random();
                RigidBody body;
                world.addBody(body = new RigidBody(
                        true, new Cube(1000, 20, 1000),
                        new Material(0.5f, 0.5f, 0.0f)
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
                            fb.put(7, 1000);
                            fb.put(8, 20);
                            fb.put(9, 1000);
                            // color
                            fb.put(10, r);
                            fb.put(11, g);
                            fb.put(12, b);
                            fb.put(13, 1);
                        },
                        (cmd) -> {
//                            if (ReniSetup.NVIDIA) {
//                                Shaders.cubeInstanceData.upload(cmd);
//                            } else {
                            cmd.endPass();
                            Shaders.cubeInstanceData.upload(cmd);
                            cmd.beginPass(pass1, ReniSetup.GRAPHICS_CONTEXT.getChainBuffer(), extent2D[0]);
//                            }
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
                        new Material(0.5f, 0.5f, 0.0f)
                ).setPosition(
                        (float) (Math.random() * 25f - (25f / 2)),
                        (float) (Math.random() * 25f - (25f / 2)) + i / 10f,
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
                        (cmd) -> {
//                            if (ReniSetup.NVIDIA) {
//                                Shaders.cubeInstanceData.upload(cmd);
//                            } else {
                            cmd.endPass();
                            Shaders.cubeInstanceData.upload(cmd);
                            cmd.beginPass(pass1, ReniSetup.GRAPHICS_CONTEXT.getChainBuffer(), extent2D[0]);
//                            }
                        },
                        body
                ));
            }

            boolean[] inputStates = new boolean[6];

            // controls
            ReniSetup.WINDOW.addKeyboardListener(new KeyboardListener() {
                @Override
                public void keyPress(int i, int i1, int i2) {
                    if (i == GLFW.GLFW_KEY_W) inputStates[0] = true;
                    else if (i == GLFW.GLFW_KEY_S) inputStates[1] = true;
                    else if (i == GLFW.GLFW_KEY_A) inputStates[2] = true;
                    else if (i == GLFW.GLFW_KEY_D) inputStates[3] = true;
                    else if (i == GLFW.GLFW_KEY_SPACE) inputStates[4] = true;
                    else if (i == GLFW.GLFW_KEY_LEFT_SHIFT) inputStates[5] = true;
                }

                @Override
                public void keyRelease(int i, int i1, int i2) {
                    if (i == GLFW.GLFW_KEY_W) inputStates[0] = false;
                    else if (i == GLFW.GLFW_KEY_S) inputStates[1] = false;
                    else if (i == GLFW.GLFW_KEY_A) inputStates[2] = false;
                    else if (i == GLFW.GLFW_KEY_D) inputStates[3] = false;
                    else if (i == GLFW.GLFW_KEY_SPACE) inputStates[4] = false;
                    else if (i == GLFW.GLFW_KEY_LEFT_SHIFT) inputStates[5] = false;
                }

                @Override
                public void keyType(int i, int i1, int i2) {

                }
            });
            ReniSetup.WINDOW.captureMouse();

            Vector3f cameraPos = new Vector3f();
            Quaternionf cameraRotation = new Quaternionf(0, 0, 0, 1);

            double[] last = new double[2];
            double[] rV = new double[2];
            ReniSetup.WINDOW.addMouseListener((v, v1) -> {
                double x = v - last[0];
                double y = v1 - last[1];
                last[0] = v;
                last[1] = v1;

                rV[0] += x;
                rV[1] -= y;

                Quaternionf q = new Quaternionf(0, 0, 0, 1);
                q.rotateLocalY((float) rV[0] / 200f);
                q.rotateLocalX((float) rV[1] / 200f);
                cameraRotation.set(q);
                cameraRotation.normalize();

//                Quaternionf q = new Quaternionf(0, 0, 0, 1);
//                Quaternionf q1 = new Quaternionf(0, 0, 0, 1);
//                q.rotateLocalY((float) (x / 200f));
//                q1.rotateLocalX((float) (y / 200f));
//                cameraRotation.conjugate();
//                cameraRotation.mul(q1);
//                cameraRotation.normalize();
//                cameraRotation.conjugate();
//                cameraRotation.mul(q);
//                cameraRotation.normalize();
            });

            Thread td = new Thread(() -> {
                while (true) {
                    world.tick();
                }
            });
            td.setDaemon(true);
            td.start();

            while (!ReniSetup.WINDOW.shouldClose()) {
                frame++;

                Quaternionf q = new Quaternionf(0, 0, 0, 1);
                q.rotateLocalX((float) rV[1] / 200f);
                Vector3f forward = new Vector3f(0, 0, 1);
                Vector3f right = new Vector3f(1, 0, 0);
                forward.rotate(q).normalize();
                right.rotate(q).normalize();

                q = new Quaternionf(0, 0, 0, 1);
                q.rotateLocalY((float) rV[0] / 200f);
                forward.rotate(q).normalize();
                right.rotate(q).normalize();

                forward.y = -forward.y;
                right.y = -right.y;

                if (inputStates[0]) cameraPos.add(forward);
                if (inputStates[1]) cameraPos.sub(forward);
                if (inputStates[2]) cameraPos.sub(right);
                if (inputStates[3]) cameraPos.add(right);
                if (inputStates[4]) cameraPos.y += 1.0f;
                if (inputStates[5]) cameraPos.y -= 1.0f;

                {
                    UniformData matrices = Shaders.matrices;

                    matrices.set(0, Matrices.projection(
                            (float) Math.toRadians(45),
                            ReniSetup.WINDOW.getWidth(), ReniSetup.WINDOW.getHeight(),
                            0.1f, 10000.0f
                    ));

                    Matrix4f view = new Matrix4f();
                    view.setLookAt(cameraPos.x, cameraPos.y, cameraPos.z,
                            cameraPos.x, cameraPos.y, cameraPos.z + 1,
                            0.0f, -1.0f, 0.0f);
                    Matrix4f model = new Matrix4f();
                    model.rotate(cameraRotation);

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
                            -Math.toRadians(frame / 10f), 1, 0, 0
                    ));
                    skyData.set(5, 1 / 32f);
                    skyData.setColor(6, 254, 247, 217, 255);

                    // scatter
                    skyData.set(7, 0);
                    skyData.set(8, 168 / 255f, 113 / 255f, 50 / 255f, 1);
                    skyData.set(9, 0, 0, 0);

                    // TODO: stars

                    skyData.upload();
                }

                ReniSetup.GRAPHICS_CONTEXT.prepareFrame(ReniSetup.WINDOW);
                extent2D[0] = ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents();

                buffer.begin();

                ReniSetup.GRAPHICS_CONTEXT.prepareChain(buffer);

                buffer.startLabel("Main Pass", 0.5f, 0, 0, 0.5f);
                buffer.beginPass(pass, ReniSetup.GRAPHICS_CONTEXT.getChainBuffer(), ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents());
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

                ReniSetup.GRAPHICS_CONTEXT.preparePresent(buffer);

                buffer.end();

                long nt = System.currentTimeMillis();
                ReniSetup.GRAPHICS_CONTEXT.submitFrame(buffer);
//                ReniSetup.GRAPHICS_CONTEXT.getLogical().getStandardQueue(ReniQueueType.GRAPHICS).await();

                ReniSetup.WINDOW.swapAndPollSize();
                GLFWWindow.poll();
                long tt = System.currentTimeMillis();
//                System.out.println(1000d / (tt - nt));

//                ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();
            }
            buffer.destroy();
        } catch (Throwable err) {
            err.printStackTrace();
        }

        col.destroy();
        ReniSetup.GRAPHICS_CONTEXT.depthBuffer().destroy();
        pool.destroy();
        cube.destroy();
        ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();
        shaders.destroy();
        desc0.destroy();
        pipeline1.destroy();
        pipeline0.destroy();
        pass.destroy();
        ReniSetup.GRAPHICS_CONTEXT.destroy();
        ReniSetup.WINDOW.dispose();
        GLFW.glfwTerminate();
    }
}
