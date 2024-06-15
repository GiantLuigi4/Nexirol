package tfc.test.noise.hm;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkExtent2D;
import tfc.nexirol.primitives.QuadPrimitive;
import tfc.nexirol.render.DataLayout;
import tfc.nexirol.render.ShaderAttachment;
import tfc.nexirol.render.SmartShader;
import tfc.nexirol.render.UniformData;
import tfc.nexirol.render.glsl.PreProcessor;
import tfc.nexirol.render.glsl.util.InsertDefineProcessor;
import tfc.nexirol.render.glsl.util.SequenceProcessor;
import tfc.renirol.ReniContext;
import tfc.renirol.backend.vk.util.VkUtil;
import tfc.renirol.frontend.enums.Operation;
import tfc.renirol.frontend.enums.flags.AdvanceRate;
import tfc.renirol.frontend.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.enums.format.AttributeFormat;
import tfc.renirol.frontend.enums.masks.DynamicStateMasks;
import tfc.renirol.frontend.enums.modes.CullMode;
import tfc.renirol.frontend.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.framebuffer.Framebuffer;
import tfc.renirol.frontend.rendering.pass.RenderPassInfo;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.itf.ReniDestructable;
import tfc.renirol.util.ShaderCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeightmapShader implements ReniDestructable {
    final ReniLogicalDevice device;

    VkExtent2D passExtents;
    int boundPipe = -1;

    final SmartShader noiseShader;
    final SmartShader blitShader;
    final PipelineState state;
    final GraphicsPipeline noisePipe;
    final GraphicsPipeline blitPipe;

    @Override
    public void destroy() {
        for (ShaderAttachment shaderAttachment : attachmentsNoise) shaderAttachment.destroy();
        for (ShaderAttachment shaderAttachment : attachmentsBlit) shaderAttachment.destroy();
        quad.destroy();
        noisePipe.destroy();
        noiseShader.destroy();
        blitPipe.destroy();
        blitShader.destroy();
        d0.destroy();
    }

    private static final byte[] vsh = VkUtil.managed(VkUtil.read(
            HeightmapShader.class.getClassLoader().getResourceAsStream("shader/internal/heightmap.vsh")
    ), (obj) -> {
        byte[] data;
        obj.get(data = new byte[obj.capacity()]);
        return data;
    }, (obj) -> MemoryUtil.memFree(obj));
    private static final byte[] fsh = VkUtil.managed(VkUtil.read(
            HeightmapShader.class.getClassLoader().getResourceAsStream("shader/internal/heightmap.fsh")
    ), (obj) -> {
        byte[] data;
        obj.get(data = new byte[obj.capacity()]);
        return data;
    }, (obj) -> MemoryUtil.memFree(obj));

    final QuadPrimitive quad;

    private static final DataElement UV = new DataElement(NumericPrimitive.FLOAT, 2);
    private static final DataElement regionStart = new DataElement(NumericPrimitive.INT, 2);
    private static final DataElement regionSize = new DataElement(NumericPrimitive.INT, 2);
    private static final DataElement passSize = new DataElement(NumericPrimitive.INT, 2);
    private static final DataFormat format = new DataFormat(UV);
    private static final DataFormat formatSize = new DataFormat(regionStart, regionSize, passSize);
    private static final BufferDescriptor descriptor = new BufferDescriptor(format);

    static {
        descriptor.advance(AdvanceRate.PER_VERTEX)
                .attribute(0, 0, AttributeFormat.RG32_FLOAT, 0);
        descriptor.describe(0);
    }

    private final UniformData d0 = new UniformData(
            false,
            DataLayout.STANDARD,
            formatSize, new ShaderStageFlags[]{ShaderStageFlags.VERTEX},
            0
    );

    final ShaderAttachment[] attachmentsNoise;
    final ShaderAttachment[] attachmentsBlit;

    public HeightmapShader(
            RenderPassInfo compatiblePass,
            ReniContext context,

            PreProcessor processor, ShaderCompiler compiler,
            String source, String file,

            UniformData... uniforms
    ) {
        ReniLogicalDevice logical = context.getLogical();
        this.device = logical;
        d0.setup(context, true);

        List<UniformData> dats = new ArrayList<>();
        dats.add(d0);
        dats.addAll(Arrays.asList(uniforms));
        uniforms = dats.toArray(new UniformData[0]);

        quad = new QuadPrimitive(
                logical, descriptor.format,
                1, 1, true
        );

        source += new String(fsh);

        PreProcessor def;
        noiseShader = new SmartShader(
                logical,
                attachmentsNoise = new ShaderAttachment[]{
                        new ShaderAttachment(
                                new SequenceProcessor(
                                        processor, def = new InsertDefineProcessor("NOISE")
                                ),
                                compiler, logical,
                                ShaderStageFlags.FRAGMENT, source,
                                file, "main"
                        ),
                        new ShaderAttachment(
                                def,
                                compiler, logical,
                                ShaderStageFlags.VERTEX,
                                new String(vsh), "heightmap.vsh",
                                "main"
                        )
                },
                uniforms
        );
        noiseShader.prepare();
        blitShader = new SmartShader(
                logical,
                attachmentsBlit = new ShaderAttachment[]{
                        new ShaderAttachment(
                                new SequenceProcessor(
                                        processor, def = new InsertDefineProcessor("NOISE")
                                ),
                                compiler, logical,
                                ShaderStageFlags.FRAGMENT, source,
                                file, "main"
                        ),
                        new ShaderAttachment(
                                def,
                                compiler, logical,
                                ShaderStageFlags.VERTEX,
                                new String(vsh), "heightmap.vsh",
                                "main"
                        )
                },
                uniforms
        );
        blitShader.prepare();
        state = new PipelineState(logical);
        state.dynamicState(DynamicStateMasks.SCISSOR, DynamicStateMasks.VIEWPORT, DynamicStateMasks.CULL_MODE);
        state.depthMask(false);
        noiseShader.bind(state, descriptor);
        noisePipe = new GraphicsPipeline(compatiblePass, state, noiseShader.shaders);
        blitShader.bind(state, descriptor);
        blitPipe = new GraphicsPipeline(compatiblePass, state, blitShader.shaders);
    }

    private RenderPassInfo info;
    private Framebuffer fbo;

    public void beginCompute(
            CommandBuffer buffer,
            Framebuffer target,
            VkExtent2D extents
    ) {
        buffer.startLabel(
                "Compute Heightmap",
                0, 0.5f, 0, 1
        );
        info = target.genericPass(
                device,
                Operation.PERFORM, Operation.PERFORM
        );
        buffer.beginPass(
                info, this.fbo = target, extents
        );
        passExtents = extents;
        boundPipe = -1;
        d0.setI(2, passExtents.width(), passExtents.height());
    }

    public void computeNoise(
            CommandBuffer commandBuffer,
            int regionX, int regionY,
            int imgX, int imgY,
            int computeWidth, int computeHeight
    ) {
        if (boundPipe != 0) {
            commandBuffer.bindPipe(noisePipe);
            commandBuffer.viewport(
                    0, 0,
                    passExtents.width(), passExtents.height(),
                    0, 1
            );
            boundPipe = 0;
            noiseShader.bindCommand(noisePipe, commandBuffer);
            quad.bind(commandBuffer);
            commandBuffer.cullMode(CullMode.NONE);
        }
        commandBuffer.viewportScissor(
                imgX, imgY,
                computeWidth, computeHeight,
                0, 1
        );

        commandBuffer.endPass();
        d0.setI(0, regionX + imgX, regionY + imgY);
        d0.setI(1, computeWidth, computeHeight);
        d0.upload(commandBuffer);

        commandBuffer.beginPass(info, fbo, passExtents);
        quad.draw(commandBuffer);
    }

    public void computeOverlay(
            CommandBuffer commandBuffer,
            int x, int y,
            int regionX, int regionY,
            int regionWidth, int regionHeight
    ) {
        if (boundPipe != 1) {
            commandBuffer.bindPipe(blitPipe);
            commandBuffer.viewport(
                    0, 0,
                    passExtents.width(), passExtents.height(),
                    0, 1
            );
            commandBuffer.scissor(
                    regionX, regionY,
                    regionWidth, regionHeight
            );
            blitShader.bindCommand(blitPipe, commandBuffer);
            quad.bind(commandBuffer);
            boundPipe = 1;
        }
        // !COMPUTE TODO
    }

    public void finishCompute(
            CommandBuffer commandBuffer
    ) {
        commandBuffer.endPass();
        commandBuffer.endLabel();
        boundPipe = -1;
        info.free();
    }
}
