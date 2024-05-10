package tfc.nexirol.render;

import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.hardware.util.ReniDestructable;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.command.shader.Shader;
import tfc.renirol.frontend.rendering.enums.BindPoint;
import tfc.renirol.frontend.rendering.enums.DescriptorType;
import tfc.renirol.frontend.rendering.enums.flags.DescriptorPoolFlags;
import tfc.renirol.frontend.rendering.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayout;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayoutInfo;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorPool;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorSet;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: move to reni
public class SmartShader implements ReniDestructable {
    public Shader[] shaders;
    UniformData[] data;

    DescriptorLayout layout;
    ReniLogicalDevice device;

    final DescriptorPool pool;
    DescriptorSet set;
    DescriptorLayoutInfo[] infos;
    BufferDescriptor[] descriptors;
    UniformData[] vbos;

    UniformData constants;

    public SmartShader(
            ReniLogicalDevice device,
            ShaderAttachment[] attachments,
            UniformData... data
    ) {
        ArrayList<DescriptorLayoutInfo> infos = new ArrayList<>();
        ArrayList<UniformData> vbo = new ArrayList<>();
        ArrayList<BufferDescriptor> descs = new ArrayList<>();
        for (UniformData datum : data)
            if (datum.descriptor != null) {
                descs.add(datum.descriptor);
                vbo.add(datum);
            } else if (datum.info != null)
                infos.add(datum.info);
            else
                constants = datum;
        this.infos = infos.toArray(new DescriptorLayoutInfo[0]);
        this.vbos = vbo.toArray(new UniformData[0]);
        this.descriptors = descs.toArray(new BufferDescriptor[0]);

        pool = new DescriptorPool(
                device,
                1,
                new DescriptorPoolFlags[0],
                DescriptorPool.PoolInfo.of(DescriptorType.UNIFORM_BUFFER, this.infos.length)
        );

        this.device = device;
        shaders = new Shader[attachments.length];
        for (int i = 0; i < shaders.length; i++) {
            shaders[i] = attachments[i].shader;
        }
        this.data = data;

        layout = new DescriptorLayout(device, 0, this.infos);

        set = new DescriptorSet(
                device,
                pool, layout
        );
    }

    public void destroy() {
    }

    public void prepare() {
        for (UniformData datum : data) {
            if (datum.info != null) {
                set.bind(
                        datum.binding,
                        0,
                        DescriptorType.UNIFORM_BUFFER,
                        datum.buffer
                );
            }
        }
    }

    public void bindCommand(GraphicsPipeline pipeline, CommandBuffer buffer) {
        buffer.bindDescriptor(
                BindPoint.GRAPHICS,
                pipeline,
                set
        );
        if (constants != null) {
            buffer.pushConstants(
                    pipeline.layout.handle,
                    constants.stages,
                    0, constants.bytes.capacity(),
                    constants.bytes.position(0).limit(constants.bytes.capacity())
            );
        }
        for (UniformData vbo : vbos) {
            buffer.bindVbo(vbo.binding, vbo.buffer);
        }
    }

    public void bind(PipelineState state, BufferDescriptor... descriptors) {
        state.descriptorLayouts(layout);
        ArrayList<BufferDescriptor> collected = new ArrayList<>();
        collected.addAll(Arrays.asList(descriptors));
        collected.addAll(Arrays.asList(this.descriptors));
        state.vertexInput(collected.toArray(new BufferDescriptor[0]));
    }
}
