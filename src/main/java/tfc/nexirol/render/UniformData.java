package tfc.nexirol.render;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryUtil;
import tfc.renirol.ReniContext;
import tfc.renirol.frontend.enums.BufferUsage;
import tfc.renirol.frontend.enums.DescriptorType;
import tfc.renirol.frontend.enums.flags.AdvanceRate;
import tfc.renirol.frontend.enums.flags.ShaderStageFlags;
import tfc.renirol.frontend.enums.format.AttributeFormat;
import tfc.renirol.frontend.enums.masks.AccessMask;
import tfc.renirol.frontend.enums.masks.StageMask;
import tfc.renirol.frontend.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.resource.buffer.BufferDescriptor;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.rendering.resource.descriptor.DescriptorLayoutInfo;
import tfc.renirol.frontend.rendering.resource.descriptor.ImageInfo;
import tfc.renirol.itf.ReniDestructable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

// TODO: move to reni
public class UniformData implements ReniDestructable {
    final DataFormat format;
    final ShaderStageFlags[] stages;
    final int binding;

    GPUBuffer buffer;
    ByteBuffer bytes;
    AccessMask mask;

    record IndexedElement(int memoryOffset, DataElement element) {

    }

    IndexedElement[] indexedElements;

    DescriptorLayoutInfo info;
    BufferDescriptor descriptor;
    ImageInfo[] samps;

    public UniformData(DataLayout layout, int textureCount, ShaderStageFlags[] stages, int binding) {
        this.format = null;
        this.stages = stages;
        this.binding = binding;
        switch (layout) {
            case COMBINED_TEXTURE_SAMPLER -> {
            }
            default -> throw new RuntimeException("Not supported");
        }

        info = new DescriptorLayoutInfo(
                binding,
                switch (layout) {
                    case COMBINED_TEXTURE_SAMPLER -> DescriptorType.COMBINED_SAMPLED_IMAGE;
                    default -> DescriptorType.UNIFORM_BUFFER;
                },
                textureCount, stages
        );
        samps = new ImageInfo[textureCount];
    }

    public UniformData(boolean constant, DataLayout layout, DataFormat format, ShaderStageFlags[] stages, int binding) {
        this(constant, layout, format, stages, binding, 0);
    }

    public UniformData(boolean constant, DataLayout layout, DataFormat format, ShaderStageFlags[] stages, int binding, int location) {
        this.format = format;
        this.stages = stages;
        this.binding = binding;
        int offset = 0;
        ArrayList<IndexedElement> elements = new ArrayList<>();
//        System.out.println();
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
//                System.out.println(offset);
                offset += element.size * element.type.size;
            }
        }
        indexedElements = elements.toArray(new IndexedElement[0]);

        if (!constant) {
            if (
                    layout == DataLayout.VERTEX ||
                            layout == DataLayout.INSTANCE
            ) {
                int strideDiv = 1;
                boolean single = false;
                if (format.elements.isStart() && format.elements.isEnd()) {
                    strideDiv = format.elements.value.arrayCount;
                    single = true;
                }

                descriptor = new BufferDescriptor(
                        format
                ).advance(switch (layout) {
                    case VERTEX -> AdvanceRate.PER_VERTEX;
                    case INSTANCE -> AdvanceRate.PER_INSTANCE;
                    default -> throw new RuntimeException("HUH???");
                });
                if (single) {
                    descriptor.describe(
                            binding,
                            ((indexedElements[indexedElements.length - 1].memoryOffset + (
                                    indexedElements[indexedElements.length - 1].element.size *
                                            indexedElements[indexedElements.length - 1].element.type.size))
                                    / strideDiv
                            )
                    );
                } else descriptor.describe(binding, format.stride);

                int idx = location;
                for (IndexedElement element : elements) {
                    descriptor.attribute(
                            binding,
                            idx++,
                            AttributeFormat.format(
                                    element.element.size,
                                    element.element.type
                            ),
                            element.memoryOffset
                    );
                }
            } else {
                info = new DescriptorLayoutInfo(
                        binding,
                        switch (layout) {
                            case COMBINED_TEXTURE_SAMPLER -> DescriptorType.COMBINED_SAMPLED_IMAGE;
                            default -> DescriptorType.UNIFORM_BUFFER;
                        },
                        1, stages
                );
                if (
                        layout == DataLayout.COMBINED_TEXTURE_SAMPLER
                ) {
                    samps = new ImageInfo[format.elements.value.arrayCount];
                }
            }
        }
        switch (layout) {
            case VERTEX, INSTANCE -> mask = AccessMask.VERTEX_READ;
            case COMBINED_TEXTURE_SAMPLER -> mask = null; // no buffer
            default -> mask = AccessMask.UNIFORM_READ;
        }
    }

    public BufferDescriptor getDescriptor() {
        return descriptor;
    }

    public UniformData(boolean constant, DataFormat format, ShaderStageFlags[] stages, int binding) {
        this(constant, DataLayout.STANDARD, format, stages, binding);
    }

    public UniformData(DataFormat format, ShaderStageFlags[] stages, int binding) {
        this(false, DataLayout.STANDARD, format, stages, binding);
    }

    public void setup(ReniContext context) {
        setup(context, false);
    }

    public void setup(ReniContext context, boolean transfer) {
        if (mask == null)
            return;

        if (info != null || descriptor != null) {
            BufferUsage usage = transfer ? BufferUsage.UNIFORM_TRANSFER : BufferUsage.UNIFORM;
            if (descriptor != null) usage = transfer ? BufferUsage.VERTEX_TRANSFER : BufferUsage.VERTEX;

            buffer = new GPUBuffer(
                    context.getLogical(),
                    usage,
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

    public void alignUL(int start, int end) {
        ulStart = Math.min(ulStart, start);
        ulEnd = Math.max(ulEnd, end);
    }

    public void alignUL(int index) {
        IndexedElement indexed = indexedElements[index];
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        int sz = indexed.element.size * indexed.element.type.size;
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + sz);
    }

    float oneDiv255 = 1 / 255f;

    public void setColor(int index, int r, int g, int b) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 3 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, r * oneDiv255);
        fb.put(1, g * oneDiv255);
        fb.put(2, b * oneDiv255);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (3 * 4));
        bytes.position(0);
    }

    public void setColor(int index, int r, int g, int b, int a) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 4 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, r * oneDiv255);
        fb.put(1, g * oneDiv255);
        fb.put(2, b * oneDiv255);
        fb.put(3, a * oneDiv255);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4));
        bytes.position(0);
    }

    public void setB(int index, byte x) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 1 || indexed.element.type != NumericPrimitive.BYTE)
            throw new RuntimeException("Invalid element type.");
        ByteBuffer fb = bytes.position(indexed.memoryOffset);
        fb.put(0, x);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + 1);
        bytes.position(0);
    }

    public void setB(int index, byte x, byte y) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 2 || indexed.element.type != NumericPrimitive.BYTE)
            throw new RuntimeException("Invalid element type.");
        ByteBuffer fb = bytes.position(indexed.memoryOffset);
        fb.put(0, x);
        fb.put(1, y);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + 2);
        bytes.position(0);
    }

    public void setB(int index, byte x, byte y, byte z) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 3 || indexed.element.type != NumericPrimitive.BYTE)
            throw new RuntimeException("Invalid element type.");
        ByteBuffer fb = bytes.position(indexed.memoryOffset);
        fb.put(0, x);
        fb.put(1, y);
        fb.put(2, z);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + 3);
        bytes.position(0);
    }

    public void setB(int index, byte x, byte y, byte z, byte w) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 4 || indexed.element.type != NumericPrimitive.BYTE)
            throw new RuntimeException("Invalid element type.");
        ByteBuffer fb = bytes.position(indexed.memoryOffset);
        fb.put(0, x);
        fb.put(1, y);
        fb.put(2, z);
        fb.put(3, w);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + 4);
        bytes.position(0);
    }

    public void setI(int index, int x) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 1 || indexed.element.type != NumericPrimitive.INT)
            throw new RuntimeException("Invalid element type.");
        IntBuffer fb = bytes.position(indexed.memoryOffset).asIntBuffer();
        fb.put(0, x);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4));
        bytes.position(0);
    }

    public void setI(int index, int x, int y) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 2 || indexed.element.type != NumericPrimitive.INT)
            throw new RuntimeException("Invalid element type.");
        IntBuffer fb = bytes.position(indexed.memoryOffset).asIntBuffer();
        fb.put(0, x);
        fb.put(1, y);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (2 * 4));
        bytes.position(0);
    }

    public void setI(int index, int x, int y, int z) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 3 || indexed.element.type != NumericPrimitive.INT)
            throw new RuntimeException("Invalid element type.");
        IntBuffer fb = bytes.position(indexed.memoryOffset).asIntBuffer();
        fb.put(0, x);
        fb.put(1, y);
        fb.put(2, z);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (3 * 4));
        bytes.position(0);
    }

    public void setI(int index, int x, int y, int z, int w) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 4 || indexed.element.type != NumericPrimitive.INT)
            throw new RuntimeException("Invalid element type.");
        IntBuffer fb = bytes.position(indexed.memoryOffset).asIntBuffer();
        fb.put(0, x);
        fb.put(1, y);
        fb.put(2, z);
        fb.put(3, w);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4));
        bytes.position(0);
    }

    public void setF(int index, float f) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 1 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        bytes.position(indexed.memoryOffset).asFloatBuffer().put(0, f);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4));
        bytes.position(0);
    }

    public void setF(int index, float x, float y) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != 2 || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        FloatBuffer fb = bytes.position(indexed.memoryOffset).asFloatBuffer();
        fb.put(0, x);
        fb.put(1, y);
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (2 * 4));
        bytes.position(0);
    }

    public void setF(int index, float x, float y, float z) {
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

    public void setF(int index, float x, float y, float z, float w) {
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

    public void setF(int index, Matrix4f matrix4f) {
        IndexedElement indexed = indexedElements[index];
        if (indexed.element.size != (4 * 4) || indexed.element.type != NumericPrimitive.FLOAT)
            throw new RuntimeException("Invalid element type.");
        matrix4f.get(bytes.position(indexed.memoryOffset));
        ulStart = Math.min(ulStart, indexed.memoryOffset);
        ulEnd = Math.max(ulEnd, indexed.memoryOffset + (4 * 4 * 4));
        bytes.position(0);
    }

    public void setF(int index, Quaternionf quat) {
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


    /**
     * CTS: combined texture sampler
     *
     * @param index
     * @param sampler
     */
    public void setCTS(int index, ImageInfo sampler) {
        if (samps == null)
            throw new RuntimeException("Invalid data layout");

        this.samps[index] = sampler;
    }

    public void upload() {
        if (buffer != null) {
            bytes.position(0).limit(ulEnd);
            buffer.upload(ulStart, (ulEnd - ulStart), bytes);
            ulStart = Integer.MAX_VALUE;
            ulEnd = 0;
        }
    }

    public void upload(CommandBuffer buffer) {
        if (this.buffer != null) {
            bytes.position(0).limit(ulEnd);

            // TODO: should use a submit queue and copy from a temporary submission buffer to the main buffer or smth
            buffer.transitionBuffer(
                    this.buffer,
                    StageMask.GRAPHICS, StageMask.TRANSFER,
                    mask, AccessMask.TRANSFER_WRITE
            );
            buffer.bufferData(
                    this.buffer, ulStart,
                    (ulEnd - ulStart), bytes
            );
            buffer.transitionBuffer(
                    this.buffer,
                    StageMask.TRANSFER, StageMask.GRAPHICS,
                    AccessMask.TRANSFER_WRITE, mask
            );
            ulStart = Integer.MAX_VALUE;
            ulEnd = 0;
            bytes.position(0).limit(bytes.capacity());
        }
    }

    @Override
    public void destroy() {
        if (descriptor != null)
            descriptor.destroy();
        if (info != null)
            info.destroy();
        if (buffer != null)
            buffer.destroy();
        if (bytes != null)
            MemoryUtil.memFree(bytes);
    }
}
