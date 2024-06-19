package tfc.test.noise;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkExtent2D;
import tfc.nexirol.math.Matrices;
import tfc.nexirol.primitives.CubePrimitive;
import tfc.nexirol.render.DataLayout;
import tfc.nexirol.render.UniformData;
import tfc.renirol.frontend.enums.ImageLayout;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.enums.flags.SwapchainUsage;
import tfc.renirol.frontend.enums.format.AttributeFormat;
import tfc.renirol.frontend.enums.masks.AccessMask;
import tfc.renirol.frontend.enums.masks.DynamicStateMasks;
import tfc.renirol.frontend.enums.masks.StageMask;
import tfc.renirol.frontend.enums.modes.CullMode;
import tfc.renirol.frontend.enums.modes.PrimitiveType;
import tfc.renirol.frontend.enums.modes.image.FilterMode;
import tfc.renirol.frontend.enums.modes.image.MipmapMode;
import tfc.renirol.frontend.enums.modes.image.WrapMode;
import tfc.renirol.frontend.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.hardware.device.ReniQueueType;
import tfc.renirol.frontend.rendering.ReniQueue;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.framebuffer.Attachment;
import tfc.renirol.frontend.rendering.framebuffer.Framebuffer;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.descriptor.ImageInfo;
import tfc.renirol.frontend.rendering.resource.image.Image;
import tfc.renirol.frontend.rendering.resource.image.texture.TextureSampler;
import tfc.renirol.frontend.windowing.glfw.GLFWWindow;
import tfc.renirol.frontend.windowing.listener.KeyboardListener;
import tfc.renirol.itf.ReniDestructable;
import tfc.renirol.util.ShaderCompiler;
import tfc.test.data.VertexElements;
import tfc.test.data.VertexFormats;
import tfc.test.noise.geom.QuadPointGrid;
import tfc.test.noise.hm.Heightmap;
import tfc.test.noise.hm.HeightmapShader;
import tfc.test.shared.ReniSetup;
import tfc.test.shared.Scenario;
import tfc.test.shared.Shaders;

public class HMTest1 {
    public static void main(String[] args) {
//        System.setProperty("joml.nounsafe", "true");
        Scenario.useWinNT = false;
//        Scenario.useRenderDoc = false;
        ReniSetup.initialize();


        Shaders shaders = new Shaders();
        ShaderCompiler compiler = new ShaderCompiler();
        compiler.setupGlsl();
        compiler.debug();


        // === Setup Heightmap Uniform Data ===
        DataElement MAP = new DataElement(NumericPrimitive.FLOAT, 2);
        DataElement TEX = new DataElement(NumericPrimitive.FLOAT, 2);
        DataFormat hmDataFormat = new DataFormat(MAP, TEX);
        UniformData hmData = new UniformData(
                false,
                DataLayout.STANDARD,
                hmDataFormat,
                new ShaderStageFlags[]{ShaderStageFlags.FRAGMENT},
                1
        );
        hmData.setup(ReniSetup.GRAPHICS_CONTEXT, true);

        // === Create Heightmap Image ===
//        int res = 2048 * 16;
//        int res = 2048 * 8;
//        int res = 2048 * 6;
        int res = 2048 * 4;
//        int res = 256 * 4;
//        int res = 64 * 64;
//        int res = 1024;
        Image img = new Image(ReniSetup.GRAPHICS_CONTEXT.getLogical());
        img.setUsage(SwapchainUsage.COLOR, SwapchainUsage.SAMPLED);
        img.create(res, res, VK13.VK_FORMAT_R16_UNORM);
        Attachment attachment = new Attachment(img, false, false);
        Framebuffer fbo = new Framebuffer(attachment);
        RenderPassInfo heightmapPass = fbo.genericPass(ReniSetup.GRAPHICS_CONTEXT.getLogical(), Operation.PERFORM, Operation.PERFORM);

        // === Create Heightmap Handler ===
        HeightmapShader shader = new HeightmapShader(
                heightmapPass,
                ReniSetup.GRAPHICS_CONTEXT,
                Shaders.processor,
                compiler,
                Shaders.read(Shaders.class.getClassLoader().getResourceAsStream("shader/heightmap/heightmap.hsh")),
                "heightmap.hsh",
                hmData
        );
        Heightmap map = new Heightmap(
                shader, res, res,
                img, fbo, heightmapPass
        );
        TextureSampler sampler = map.createSampler(
                WrapMode.REPEAT, WrapMode.REPEAT,
                FilterMode.NEAREST, FilterMode.LINEAR,
                MipmapMode.NEAREST,
                false, 0,
                0, 0, 0
        );
        TextureSampler samplerNearest = map.createSampler(
                WrapMode.REPEAT, WrapMode.REPEAT,
                FilterMode.NEAREST, FilterMode.NEAREST,
                MipmapMode.NEAREST,
                false, 0,
                0, 0, 0
        );
        Shaders.terrainTextureData.setCTS(0, new ImageInfo(img, sampler));
        Shaders.terrainTextureDataNearest.setCTS(0, new ImageInfo(img, samplerNearest));


        final RenderPassInfo pass = ReniSetup.GRAPHICS_CONTEXT.getPass(
                Operation.CLEAR, Operation.PERFORM,
                ImageLayout.PRESENT
        );

        PipelineState state = new PipelineState(ReniSetup.GRAPHICS_CONTEXT.getLogical());
        state.dynamicState(DynamicStateMasks.SCISSOR, DynamicStateMasks.VIEWPORT, DynamicStateMasks.CULL_MODE);

        DataFormat formatSky = VertexFormats.POS4_NORMAL3;

        final BufferDescriptor desc0 = new BufferDescriptor(formatSky);
        desc0.describe(0);
        desc0.attribute(0, 0, AttributeFormat.RGBA32_FLOAT, 0);
        desc0.attribute(0, 1, AttributeFormat.RGB32_FLOAT, formatSky.offset(VertexElements.NORMAL_XYZ));

        shaders.SKY.prepare();
        shaders.SKY.bind(state, desc0);
        state.depthTest(false).depthMask(false);
        GraphicsPipeline pipeline0 = new GraphicsPipeline(pass, state, shaders.SKY.shaders);
        shaders.TERRAIN_HEIGHTMAP.prepare();
        shaders.TERRAIN_HEIGHTMAP.bind(state);
        state.depthTest(true).depthMask(true);
        state.setTopology(PrimitiveType.PATCH).patchControlPoints(4);
        GraphicsPipeline pipeline1 = new GraphicsPipeline(pass, state, shaders.TERRAIN_HEIGHTMAP.shaders);

        CubePrimitive cube = new CubePrimitive(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                formatSky, 1, 1, 1
        );

        int GRID = 128;
        QuadPointGrid quad = new QuadPointGrid(
                ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                1, 1,
                GRID, GRID, false
        );
        GRID *= 64;

        ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();

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

            VkExtent2D[] extent2D = new VkExtent2D[1];

            boolean[] inputStates = new boolean[10];

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
                    else if (i == GLFW.GLFW_KEY_V) inputStates[6] = true;
                    else if (i == GLFW.GLFW_KEY_X) inputStates[7] = true;
                    else if (i == GLFW.GLFW_KEY_Z) inputStates[8] = true;
                    else if (i == GLFW.GLFW_KEY_C) inputStates[9] = true;
                }

                @Override
                public void keyRelease(int i, int i1, int i2) {
                    if (i == GLFW.GLFW_KEY_W) inputStates[0] = false;
                    else if (i == GLFW.GLFW_KEY_S) inputStates[1] = false;
                    else if (i == GLFW.GLFW_KEY_A) inputStates[2] = false;
                    else if (i == GLFW.GLFW_KEY_D) inputStates[3] = false;
                    else if (i == GLFW.GLFW_KEY_SPACE) inputStates[4] = false;
                    else if (i == GLFW.GLFW_KEY_LEFT_SHIFT) inputStates[5] = false;
                    else if (i == GLFW.GLFW_KEY_V) inputStates[6] = false;
                    else if (i == GLFW.GLFW_KEY_X) inputStates[7] = false;
                    else if (i == GLFW.GLFW_KEY_Z) inputStates[8] = false;
                    else if (i == GLFW.GLFW_KEY_C) inputStates[9] = false;
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
//                rV[0] = Math.toRadians(45) * 200;

                Quaternionf q = new Quaternionf(0, 0, 0, 1);
                q.rotateLocalY((float) rV[0] / 200f);
                q.rotateLocalX((float) rV[1] / 200f);
                cameraRotation.set(q);
                cameraRotation.normalize();
            });

            CommandBuffer cmd = CommandBuffer.create(
                    ReniSetup.GRAPHICS_CONTEXT.getLogical(),
                    ReniQueueType.GRAPHICS, true,
                    false, false
            );
            {
                cmd.begin();

                // === Compute Heightmap ===
                hmData.setF(0, 0, 1);
                hmData.setF(1, 0, 1);
                hmData.upload(cmd);
                map.prepare(cmd, 0, 0);

                cmd.transition(
                        img.getHandle(),
                        StageMask.COLOR_ATTACHMENT_OUTPUT, StageMask.GRAPHICS,
                        ImageLayout.COLOR_ATTACHMENT_OPTIMAL, ImageLayout.SHADER_READONLY,
                        AccessMask.COLOR_WRITE, AccessMask.SHADER_READ
                );

                cmd.end();
                cmd.submitBlocking(
                        ReniSetup.GRAPHICS_CONTEXT.getLogical().getStandardQueue(ReniQueueType.GRAPHICS),
                        StageMask.GRAPHICS
                );
                cmd.reset();
            }

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

                for (int i = 0; i < 100; i++) {
                    if (inputStates[6]) cameraPos.add(256 * 3, 0, 256 * 3);
                    if (inputStates[7]) cameraPos.add(256 * 3, 0, -256 * 3);
                    if (inputStates[8]) cameraPos.add(-256 * 3, 0, -256 * 3);
                    if (inputStates[9]) cameraPos.add(-256 * 3, 0, 256 * 3);
                }

                {
                    UniformData matrices = Shaders.matrices;
                    matrices.setF(0, Matrices.projection(
                            (float) Math.toRadians(45),
                            ReniSetup.WINDOW.getWidth(), ReniSetup.WINDOW.getHeight(),
                            0.1f, 10000.0f
                    ));

                    Matrix4f view = new Matrix4f();
                    view.setLookAt(cameraPos.x, cameraPos.y, cameraPos.z,
                            cameraPos.x, cameraPos.y, cameraPos.z + 100,
                            0.0f, -1.0f, 0.0f);
                    Matrix4f model = new Matrix4f();
                    model.rotate(cameraRotation);

                    model.mul(view);
                    matrices.setF(1, model);
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
                    skyData.setF(4, new Quaternionf().setAngleAxis(
                            Math.toRadians(frame / 10f), 1, 0, 0
                    ));
                    skyData.setF(5, 1 / 32f);
                    skyData.setColor(6, 254, 247, 217, 255);

                    // scatter
                    skyData.setF(7, 0);
                    skyData.setF(8, 168 / 255f, 113 / 255f, 50 / 255f, 1);
                    skyData.setF(9, 0, 0, 0);

                    // TODO: stars

                    skyData.upload();
                }

                final int GRID_1 = 64;
                int mapX = (int) (cameraPos.x / (GRID_1 * 3)) * GRID_1;
                int mapY = (int) (cameraPos.z / (GRID_1 * 3)) * GRID_1;

                boolean mapUpdated = false;
                if (map.needsUpdate(mapX, mapY)) {
                    mapUpdated = true;
                    cmd.begin();
                    map.updatePosition(cmd, mapX, mapY);
                    cmd.end();
                    cmd.submitAsync(
                            ReniSetup.GRAPHICS_CONTEXT.getLogical().getStandardQueue(ReniQueueType.GRAPHICS),
                            StageMask.TOP_OF_PIPE
                    );
                }

                Shaders.heightmapData.setI(0, map.getCx(), map.getCy());
                Shaders.heightmapData.setF(1, -1000, 4000);
                Shaders.heightmapData.upload();

                ReniSetup.GRAPHICS_CONTEXT.prepareFrame(ReniSetup.WINDOW);
                extent2D[0] = ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents();

                buffer.begin();

                buffer.transition(
                        ReniSetup.GRAPHICS_CONTEXT.getFramebuffer().image,
                        StageMask.TOP_OF_PIPE,
                        StageMask.COLOR_ATTACHMENT_OUTPUT,
                        ImageLayout.UNDEFINED,
                        ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
                        AccessMask.NONE,
                        AccessMask.COLOR_WRITE
                );
                buffer.transition(
                        ReniSetup.GRAPHICS_CONTEXT.depthBuffer().getHandle(),
                        StageMask.TOP_OF_PIPE,
                        StageMask.FRAGMENT_TEST,
                        ImageLayout.UNDEFINED,
                        ImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                        AccessMask.NONE,
                        AccessMask.DEPTH_WRITE,
                        SwapchainUsage.DEPTH
                );

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
                    shaders.TERRAIN_HEIGHTMAP.bindCommand(pipeline1, buffer);
                    buffer.cullMode(CullMode.BACK);
                    buffer.viewportScissor(
                            0, 0,
                            ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().width(),
                            ReniSetup.GRAPHICS_CONTEXT.defaultSwapchain().getExtents().height(),
                            0f, 1f
                    );
                    quad.bind(buffer);
                    quad.draw(
                            buffer, pipeline1,
                            (res / GRID) *
                                    (res / GRID)
                    );
                    buffer.endLabel();
                }
                buffer.endPass();
                buffer.endLabel();

                buffer.transition(
                        ReniSetup.GRAPHICS_CONTEXT.getFramebuffer().image,
                        StageMask.COLOR_ATTACHMENT_OUTPUT,
                        StageMask.BOTTOM_OF_PIPE,
                        ImageLayout.COLOR_ATTACHMENT_OPTIMAL,
                        ImageLayout.PRESENT,
                        AccessMask.COLOR_WRITE,
                        AccessMask.NONE
                );

                buffer.end();

                ReniQueue queue = ReniSetup.GRAPHICS_CONTEXT.getLogical().getStandardQueue(ReniQueueType.GRAPHICS);
                if (mapUpdated)
                    queue.await();
                ReniSetup.GRAPHICS_CONTEXT.submitFrame(buffer);
                queue.await();

                ReniSetup.WINDOW.swapAndPollSize();
                GLFWWindow.poll();

                ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();

//                try {
//                    Thread.sleep(8);
//                } catch (Throwable err) {
//                }
            }
            buffer.destroy();
        } catch (Throwable err) {
            err.printStackTrace();
        }

        // destroy heightmap
        hmData.destroy();
        img.destroy();
        if (fbo instanceof ReniDestructable destructable)
            destructable.destroy();
        pass.destroy();
        sampler.destroy();
        samplerNearest.destroy();
        map.destroy();
        shader.destroy();

        quad.destroy();
        cube.destroy();
        ReniSetup.GRAPHICS_CONTEXT.getLogical().waitForIdle();
        shaders.destroy();
        desc0.destroy();
        pipeline1.destroy();
        pipeline0.destroy();
        pass.destroy();
        ReniSetup.GRAPHICS_CONTEXT.depthBuffer().destroy();
        ReniSetup.GRAPHICS_CONTEXT.destroy();
        ReniSetup.WINDOW.dispose();
        GLFW.glfwTerminate();
    }
}
