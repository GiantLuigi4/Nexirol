package tfc.nexirol.render;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import tfc.renirol.ReniContext;
import tfc.renirol.frontend.hardware.device.ReniQueueType;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.enums.BufferUsage;
import tfc.renirol.frontend.rendering.enums.DescriptorType;
import tfc.renirol.frontend.rendering.enums.flags.AdvanceRate;
import tfc.renirol.frontend.rendering.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.rendering.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayoutInfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

// TODO: move to reni
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
    BufferDescriptor descriptor;

    public UniformData(boolean constant, DataLayout layout, DataFormat format, ShaderStageFlags[] stages, int binding) {
        this.format = format;
        this.stages = stages;
        this.binding = binding;
        int offset = 0;
        ArrayList<IndexedElement> elements = new ArrayList<>();
        System.out.println();
        for (DataElement element : format.elements) {
            if (layout == DataLayout.STANDARD) {
                int alignment = Math.clamp(element.size, 0, 4) * element.type.size;

                // TODO: this is slightly assumption based
                if (element.size > 4) alignment = 4 * element.type.size;
                else if (element.size == 3) alignment = 4 * element.type.size;

                offset = ((offset + (alignment - 1)) & -alignment);
            }

            for (int i = 0; i < element.arrayCount; i++) {
                IndexedElement elem = new IndexedElement(offset, element);
                elements.add(elem);
                System.out.println(offset);
                offset += element.size * element.type.size;
            }
        }
        indexedElements = elements.toArray(new IndexedElement[0]);

        if (!constant) {
            if (
                    layout == DataLayout.VERTEX ||
                            layout == DataLayout.INSTANCE
            ) {
                descriptor = new BufferDescriptor(
                        format
                ).advance(switch (layout) {
                    case VERTEX -> AdvanceRate.PER_VERTEX;
                    case INSTANCE -> AdvanceRate.PER_INSTANCE;
                    default -> throw new RuntimeException("HUH???");
                });
                descriptor.describe(binding);
            } else {
                info = new DescriptorLayoutInfo(
                        binding,
                        DescriptorType.UNIFORM_BUFFER,
                        1, stages
                );
            }
        }
    }

    public UniformData(boolean constant, DataFormat format, ShaderStageFlags[] stages, int binding) {
        this(constant, DataLayout.STANDARD, format, stages, binding);
    }

    public UniformData(DataFormat format, ShaderStageFlags[] stages, int binding) {
        this(false, DataLayout.STANDARD, format, stages, binding);
    }

    public void setup(ReniContext context) {
        if (info != null) {
            buffer = new GPUBuffer(
                    context.getLogical(),
                    BufferUsage.UNIFORM,
                    indexedElements[indexedElements.length - 1].memoryOffset + (
                            indexedElements[indexedElements.length - 1].element.size *
                                    indexedElements[indexedElements.length - 1].element.type.size
                    )
            );
            buffer.allocate();
            bytes = buffer.createByteBuf();
        } else {
            bytes = MemoryUtil.memCalloc(
                    indexedElements[indexedElements.length - 1].memoryOffset + (
                            indexedElements[indexedElements.length - 1].element.size *
                                    indexedElements[indexedElements.length - 1].element.type.size
                    )
            );
        }
    }

    int ulStart = Integer.MAX_VALUE;
    int ulEnd = 0;

    public ByteBuffer get(int index) {
        IndexedElement indexed = indexedElements[index];
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        int sz = indexed.element.size * indexed.element.type.size;
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + sz);
        ByteBuffer subBuf = bytes.position(indexed.memoryOffset).limit(indexed.memoryOffset + sz);
        subBuf = MemoryUtil.memByteBuffer(
                MemoryUtil.memAddress(subBuf),
                sz
        );
        bytes.position(0).limit(bytes.capacity());
        return subBuf;
    }

    public void setColor(int index, int r, int g, int b) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 3 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, r / 255f);
        fb.put(1, g / 255f);
        fb.put(2, b / 255f);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (3 * 4));
        bytes.position(0);
    }

    public void setColor(int index, int r, int g, int b, int a) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 4 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, r / 255f);
        fb.put(1, g / 255f);
        fb.put(2, b / 255f);
        fb.put(3, a / 255f);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4));
        bytes.position(0);
    }

    public void set(int index, float x, float y, float z, float w) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 4 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, x);
        fb.put(1, y);
        fb.put(2, z);
        fb.put(3, w);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4));
        bytes.position(0);
    }

    public void set(int index, float x, float y, float z) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 3 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, x);
        fb.put(1, y);
        fb.put(2, z);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (3 * 4));
        bytes.position(0);
    }

    public void set(int index, Matrix4f matrix4f) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != (4 * 4) || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        matrix4f.get(bytes.position(indexed.memoryOffset));
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4 * 4));
        bytes.position(0);
    }

    public void set(int index, Quaternionf quat) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 4 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, quat.x);
        fb.put(1, quat.y);
        fb.put(2, quat.z);
        fb.put(3, quat.w);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4));
        bytes.position(0);
    }

    public void set(int index, float f) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 1 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        bytes.position(indexed.memoryOffset).asFloatBuffer().put(0, f);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4));
        bytes.position(0);
    }

    public void upload() {
        if (info != null) {
            bytes.position(0).limit(ulEnd);
            buffer.upload(ulStart, (ulEnd - ulStart), bytes);
            ulStart = Integer.MAX_VALUE;
            ulEnd = 0;
        }
    }

    public void upload(CommandBuffer buffer) {
        if (info != null) {
            bytes.position(0).limit(ulEnd);
            // TODO: should use a submit queue and copy from a temporary submission buffer to the main buffer or smth
            buffer.bufferData(
                    this.buffer, ulStart,
                    (ulEnd - ulStart), bytes
            );
            ulStart = Integer.MAX_VALUE;
            ulEnd = 0;
        }
    }
}
