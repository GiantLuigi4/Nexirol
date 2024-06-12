package tfc.nexirol.primitives;

import org.lwjgl.system.MemoryUtil;
import tfc.renirol.frontend.enums.BufferUsage;
import tfc.renirol.frontend.enums.IndexSize;
import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.reni.draw.batch.Drawable;
import tfc.renirol.frontend.reni.draw.instance.InstanceKey;
import tfc.renirol.frontend.reni.draw.instance.Instanceable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

public class QuadPrimitive implements Instanceable, Drawable, InstanceKey {
    private final GPUBuffer vbo;
    private final GPUBuffer ibo;
    final float width;
    final float height;
    final boolean tris;
    final int primCount;

    public int putVec3(
            DataFormat format,
            FloatBuffer buffer, int start,
            float x, float y, float z,
            float nx, float ny, float nz
    ) {
        int count = 0;
        for (DataElement element : format.elements) {
            count++;
            if (count > 2)
                throw new RuntimeException("Unsupported format");
        }

        int stride = format.elements.value.size;

        if (format.elements.value.size > 4)
            throw new RuntimeException("Unsupported format");

        if (format.elements.value.size >= 2) {
            buffer.put(start, x);
            buffer.put(start + 1, stride == 2 ? z : y);
            if (format.elements.value.size >= 3) {
                buffer.put(start + 2, z);

                if (format.elements.value.size == 4)
                    buffer.put(start + 3, 1);
            }
        } else throw new RuntimeException("Unsupported format");

        if (count == 2) {
            stride += 3;
            if (format.elements.getNext().value.size == 3) {
                buffer.put(start + 4, nx);
                buffer.put(start + 5, ny);
                buffer.put(start + 6, nz);
            } else {
                throw new RuntimeException("Unsupported format");
            }
        }

        return start + stride;
    }

    /**
     * @param format must be suitable for POSITION4, NORMAL3
     *               must be suitable for at least POSITION2
     * @param width  the width of the box
     * @param height the height of the box
     * @param tris   whether the mesh should be generated as triangles or quads
     */
    public QuadPrimitive(ReniLogicalDevice device, DataFormat format, float width, float height, boolean tris) {
        this.width = width;
        this.height = height;
        this.tris = tris;
        primCount = tris ? 6 : 4;

        vbo = new GPUBuffer(device, BufferUsage.VERTEX, format.stride * 4);
        ibo = new GPUBuffer(device, BufferUsage.INDEX, primCount * 2);

        vbo.allocate();
        ibo.allocate();

        {
            ByteBuffer buffer = vbo.createByteBuf();
            FloatBuffer fb = buffer.asFloatBuffer();

            int index = 0;
            // top/bottom
            {
                index = putVec3(
                        format,
                        fb, index,
                        -width / 2, 0, height / 2,
                        0, 1, 0
                );
                index = putVec3(
                        format,
                        fb, index,
                        -width / 2, 0, -height / 2,
                        0, 1, 0
                );
                index = putVec3(
                        format,
                        fb, index,
                        width / 2, 0, -height / 2,
                        0, 1, 0
                );
                index = putVec3(
                        format,
                        fb, index,
                        width / 2, 0, height / 2,
                        0, 1, 0
                );
            }
            vbo.upload(0, buffer);
            MemoryUtil.memFree(buffer);
        }

        {
            ByteBuffer buffer = ibo.createByteBuf();
            ShortBuffer ib = buffer.asShortBuffer();

            if (tris) {
                ib.put(0, (short) (2));
                ib.put(1, (short) (1));
                ib.put(2, (short) (0));

                ib.put(3, (short) (0));
                ib.put(4, (short) (3));
                ib.put(5, (short) (2));
            } else {
                ib.put(0, (short) (0));
                ib.put(1, (short) (1));
                ib.put(2, (short) (3));
                ib.put(3, (short) (2));
            }

            ibo.upload(0, buffer);

            MemoryUtil.memFree(buffer);
        }
    }

    public void draw(CommandBuffer buffer) {
        buffer.drawIndexed(
                0,
                0, 1,
                0, primCount
        );
    }

    public void destroy() {
        vbo.destroy();
        ibo.destroy();
    }

    @Override
    public void bind(CommandBuffer buffer) {
        buffer.bindIbo(IndexSize.INDEX_16, ibo);
        buffer.bindVbo(0, vbo);
    }

    @Override
    public void draw(CommandBuffer buffer, GraphicsPipeline graphicsPipeline, int instances) {
        buffer.drawIndexed(
                0,
                0, instances,
                0, primCount
        );
    }

    @Override
    public InstanceKey comparator() {
        return this;
    }

    @Override
    public void setup(CommandBuffer commandBuffer, int i) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuadPrimitive that = (QuadPrimitive) o;
        return width == that.width && height == that.height && tris == that.tris;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, tris);
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline, int i, int i1) {
        commandBuffer.draw(i, i1);
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline) {
        commandBuffer.draw(0, primCount);
    }

    @Override
    public int size() {
        return primCount;
    }

    @Override
    public void prepareCall(CommandBuffer commandBuffer) {
    }

    @Override
    public boolean visible() {
        return Drawable.super.visible();
    }
}
