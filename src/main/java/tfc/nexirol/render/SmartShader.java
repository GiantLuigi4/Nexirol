package tfc.nexirol.render;

import tfc.renirol.frontend.enums.BindPoint;
import tfc.renirol.frontend.enums.DescriptorType;
import tfc.renirol.frontend.enums.flags.DescriptorPoolFlags;
import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.command.pipeline.PipelineState;
import tfc.renirol.frontend.rendering.command.shader.Shader;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.descriptor.*;
import tfc.renirol.itf.ReniDestructable;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: move to reni
public class SmartShader implements ReniDestructable {
    public Shader[] shaders;
    UniformData[] data;

    DescriptorLayout layout;
    DescriptorSet set;

    DescriptorLayout layoutCTS;
    DescriptorSet setCTS;

    ReniLogicalDevice device;

    final DescriptorPool pool;
    DescriptorLayoutInfo[] infos;
    DescriptorLayoutInfo[] sampInfos;
    BufferDescriptor[] descriptors;
    UniformData[] vbos;
    UniformData[] samplers;

    UniformData constants;

    public SmartShader(
            ReniLogicalDevice device,
            ShaderAttachment[] attachments,
            UniformData... data
    ) {
        ArrayList<DescriptorLayoutInfo> infos = new ArrayList<>();
        ArrayList<UniformData> vbo = new ArrayList<>();
        ArrayList<BufferDescriptor> descs = new ArrayList<>();
        ArrayList<UniformData> samplers = new ArrayList<>();
        ArrayList<DescriptorLayoutInfo> sampInfos = new ArrayList<>();
        for (UniformData datum : data)
            if (datum.samps != null) {
                samplers.add(datum);
                sampInfos.add(datum.info);
            } else if (datum.descriptor != null) {
                descs.add(datum.descriptor);
                vbo.add(datum);
            } else if (datum.info != null)
                infos.add(datum.info);
            else
                constants = datum;
        this.infos = infos.toArray(new DescriptorLayoutInfo[0]);
        this.vbos = vbo.toArray(new UniformData[0]);
        this.descriptors = descs.toArray(new BufferDescriptor[0]);
        this.samplers = samplers.toArray(new UniformData[0]);
        this.sampInfos = sampInfos.toArray(new DescriptorLayoutInfo[0]);

        int sets = 1;
        if (!samplers.isEmpty()) sets++;

        DescriptorPool.PoolInfo[] poolInfos = new DescriptorPool.PoolInfo[sets];
        poolInfos[0] = DescriptorPool.PoolInfo.of(DescriptorType.UNIFORM_BUFFER, this.infos.length);
        int idx = 1;
        if (!samplers.isEmpty())
            //noinspection ReassignedVariable,UnusedAssignment
            poolInfos[idx++] = DescriptorPool.PoolInfo.of(DescriptorType.COMBINED_SAMPLED_IMAGE, this.sampInfos.length);

        pool = new DescriptorPool(
                device,
                sets,
                new DescriptorPoolFlags[0],
                poolInfos
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

        idx = 1;
        if (!samplers.isEmpty()) {
            //noinspection ReassignedVariable,UnusedAssignment
            layoutCTS = new DescriptorLayout(
                    device, idx++,
                    this.sampInfos
            );
            setCTS = new DescriptorSet(
                    device,
                    pool, layoutCTS
            );
        }
    }

    public void destroy() {
        if (constants != null)
            constants.destroy();
        pool.destroy();
        layout.destroy();
        if (layoutCTS != null)
            layoutCTS.destroy();
    }

    public void prepare() {
        for (UniformData datum : data) {
            if (datum.samps != null)
                continue;

            if (datum.info != null) {
                set.bind(
                        datum.binding,
                        0,
                        DescriptorType.UNIFORM_BUFFER,
                        datum.buffer
                );
            }
        }
        for (UniformData sampler : samplers) {
            int id = 0;
            for (ImageInfo samp : sampler.samps) {
                setCTS.bind(
                        sampler.binding,
                        id++, // TODO: check..?
                        DescriptorType.COMBINED_SAMPLED_IMAGE,
                        samp
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
        if (setCTS != null) {
            buffer.bindDescriptor(
                    BindPoint.GRAPHICS,
                    pipeline,
                    setCTS,
                    1
            );
        }

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
        DescriptorLayout[] layouts = new DescriptorLayout[
                1 + (layoutCTS != null ? 1 : 0)
        ];
        layouts[0] = layout;
        int idx = 1;
        if (layoutCTS != null)
            //noinspection UnusedAssignment
            layouts[idx++] = layoutCTS;

        state.descriptorLayouts(layouts);
        ArrayList<BufferDescriptor> collected = new ArrayList<>();
        collected.addAll(Arrays.asList(descriptors));
        collected.addAll(Arrays.asList(this.descriptors));
        state.vertexInput(collected.toArray(new BufferDescriptor[0]));
    }
}
