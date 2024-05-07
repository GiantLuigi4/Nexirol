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
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayout;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayoutInfo;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorPool;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorSet;

public class SmartShader implements ReniDestructable {
    public Shader[] shaders;
    UniformData[] data;

    DescriptorLayout layout;
    ReniLogicalDevice device;

    final DescriptorPool pool;
    DescriptorSet set;
    DescriptorLayoutInfo[] infos;

    public SmartShader(
            ReniLogicalDevice device,
            ShaderAttachment[] attachments,
            UniformData... data
    ) {
        pool = new DescriptorPool(
                device,
                1,
                new DescriptorPoolFlags[0],
                DescriptorPool.PoolInfo.of(DescriptorType.UNIFORM_BUFFER, 128)
        );

        this.device = device;
        shaders = new Shader[attachments.length];
        for (int i = 0; i < shaders.length; i++) {
            shaders[i] = attachments[i].shader;
        }
        this.data = data;

        infos = new DescriptorLayoutInfo[data.length];
        for (int i = 0; i < infos.length; i++)
            infos[i] = data[i].info;

        layout = new DescriptorLayout(device, 0, infos);

        set = new DescriptorSet(
                device,
                pool, layout
        );
    }

    public void destroy() {
    }

    public void prepare() {
        int i = 0;
        for (UniformData datum : data) {
            set.bind(
                    datum.binding,
                    i++,
                    DescriptorType.UNIFORM_BUFFER,
                    datum.buffer
            );
        }
    }

    public void bindCommand(GraphicsPipeline pipeline, CommandBuffer buffer) {
        buffer.bindDescriptor(
                BindPoint.GRAPHICS,
                pipeline,
                set
        );
    }

    public void bind(PipelineState state) {
        state.descriptorLayouts(layout);
    }
}
