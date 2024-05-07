package tfc.nexirol.render;

import org.joml.Matrix4f;
import tfc.renirol.ReniContext;
import tfc.renirol.frontend.rendering.enums.BufferUsage;
import tfc.renirol.frontend.rendering.enums.DescriptorType;
import tfc.renirol.frontend.rendering.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.rendering.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayoutInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class UniformData {
    final DataFormat format;
    final ShaderStageFlags[] stages;
    final int binding;

    GPUBuffer buffer;
    ByteBuffer bytes;

    record IndexedElement(int memoryOffset, DataElement element) {
    }

    IndexedElement[] indexedElements;

    DescriptorLayoutInfo info;

    public UniformData(DataFormat format, ShaderStageFlags[] stages, int binding) {
        this.format = format;
        this.stages = stages;
        this.binding = binding;
        int offset = 0;
        ArrayList<IndexedElement> elements = new ArrayList<>();
        for (DataElement element : format.elements) {
            elements.add(new IndexedElement(offset, element));
            offset += element.size * element.type.size;
        }
        indexedElements = elements.toArray(new IndexedElement[0]);

        info = new DescriptorLayoutInfo(
                binding,
                DescriptorType.UNIFORM_BUFFER,
                1, stages
        );
    }

    public void setup(ReniContext context) {
        buffer = new GPUBuffer(
                context.getLogical(),
                BufferUsage.UNIFORM,
                format.stride
        );
        buffer.allocate();
        bytes = buffer.createByteBuf();
    }

    int ulStart = Integer.MAX_VALUE;
    int ulEnd = 0;

    public void set(int index, Matrix4f matrix4f) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != (4 * 4) || indexed.element.type != NumericPrimitive.FLOAT) throw new RuntimeException("Invalid element type.");
        matrix4f.get(bytes.position(indexed.memoryOffset));
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4 * 4));
        bytes.position(0);
    }

    public void upload() {
        bytes.position(0).limit(ulEnd);
        buffer.upload(ulStart, (ulEnd - ulStart), bytes);
    }
}
