package tfc.nexirol.primitives;

import org.lwjgl.system.MemoryUtil;
import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.enums.BufferUsage;
import tfc.renirol.frontend.rendering.enums.IndexSize;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.reni.draw.batch.Drawable;
import tfc.renirol.frontend.reni.draw.instance.InstanceKey;
import tfc.renirol.frontend.reni.draw.instance.Instanceable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

public class CubePrimitive implements Instanceable, Drawable, InstanceKey {
    private final GPUBuffer vbo;
    private final GPUBuffer ibo;
    final float width;
    final float height;
    final float length;

    public int putVec3(
            FloatBuffer buffer, int start,
            float x, float y, float z,
            float nx, float ny, float nz
    ) {
        buffer.put(start, x);
        buffer.put(start + 1, y);
        buffer.put(start + 2, z);
        buffer.put(start + 3, 1);

        buffer.put(start + 4, nx);
        buffer.put(start + 5, ny);
        buffer.put(start + 6, nz);

        return start + 7;
    }

    /**
     * @param format must be suitable for POSITION, NORMAL
     * @param width  the width of the box
     * @param height the height of the box
     * @param length the length of the box
     */
    public CubePrimitive(ReniLogicalDevice device, DataFormat format, float width, float height, float length) {
        this.width = width;
        this.height = height;
        this.length = length;

        vbo = new GPUBuffer(device, BufferUsage.VERTEX, format.stride * (4 * 6) * 4);
        ibo = new GPUBuffer(device, BufferUsage.INDEX, (6 * 6) * 2);

        vbo.allocate();
        ibo.allocate();

        {
            ByteBuffer buffer = vbo.createByteBuf();
            FloatBuffer fb = buffer.asFloatBuffer();

            int index = 0;
            // front/back
            {
                index = putVec3(
                        fb, index,
                        -width / 2, -height / 2, -length / 2,
                        0, 0, -1
                );
                index = putVec3(
                        fb, index,
                        width / 2, -height / 2, -length / 2,
                        0, 0, -1
                );
                index = putVec3(
                        fb, index,
                        width / 2, height / 2, -length / 2,
                        0, 0, -1
                );
                index = putVec3(
                        fb, index,
                        -width / 2, height / 2, -length / 2,
                        0, 0, -1
                );
            }
            {
                index = putVec3(
                        fb, index,
                        -width / 2, height / 2, length / 2,
                        0, 0, 1
                );
                index = putVec3(
                        fb, index,
                        width / 2, height / 2, length / 2,
                        0, 0, 1
                );
                index = putVec3(
                        fb, index,
                        width / 2, -height / 2, length / 2,
                        0, 0, 1
                );
                index = putVec3(
                        fb, index,
                        -width / 2, -height / 2, length / 2,
                        0, 0, 1
                );
            }
            // left/right
            {
                index = putVec3(
                        fb, index,
                        -width / 2, -height / 2, length / 2,
                        -1, 0, 0
                );
                index = putVec3(
                        fb, index,
                        -width / 2, -height / 2, -length / 2,
                        -1, 0, 0
                );
                index = putVec3(
                        fb, index,
                        -width / 2, height / 2, -length / 2,
                        -1, 0, 0
                );
                index = putVec3(
                        fb, index,
                        -width / 2, height / 2, length / 2,
                        -1, 0, 0
                );
            }
            {
                index = putVec3(
                        fb, index,
                        width / 2, height / 2, length / 2,
                        1, 0, 0
                );
                index = putVec3(
                        fb, index,
                        width / 2, height / 2, -length / 2,
                        1, 0, 0
                );
                index = putVec3(
                        fb, index,
                        width / 2, -height / 2, -length / 2,
                        1, 0, 0
                );
                index = putVec3(
                        fb, index,
                        width / 2, -height / 2, length / 2,
                        1, 0, 0
                );
            }
            // top/bottom
            {
                index = putVec3(
                        fb, index,
                        width / 2, -height / 2, length / 2,
                        0, -1, 0
                );
                index = putVec3(
                        fb, index,
                        width / 2, -height / 2, -length / 2,
                        0, -1, 0
                );
                index = putVec3(
                        fb, index,
                        -width / 2, -height / 2, -length / 2,
                        0, -1, 0
                );
                index = putVec3(
                        fb, index,
                        -width / 2, -height / 2, length / 2,
                        0, -1, 0
                );
            }
            {
                index = putVec3(
                        fb, index,
                        -width / 2, height / 2, length / 2,
                        0, 1, 0
                );
                index = putVec3(
                        fb, index,
                        -width / 2, height / 2, -length / 2,
                        0, 1, 0
                );
                index = putVec3(
                        fb, index,
                        width / 2, height / 2, -length / 2,
                        0, 1, 0
                );
                index = putVec3(
                        fb, index,
                        width / 2, height / 2, length / 2,
                        0, 1, 0
                );
            }
            vbo.upload(0, buffer);
            MemoryUtil.memFree(buffer);
        }

        {
            ByteBuffer buffer = ibo.createByteBuf();
            ShortBuffer ib = buffer.asShortBuffer();

            for (int i = 0; i < 6; i++) {
                ib.put(i * 6 + 2, (short) (i * 4));
                ib.put(i * 6 + 1, (short) (i * 4 + 1));
                ib.put(i * 6 + 0, (short) (i * 4 + 2));

                ib.put(i * 6 + 5, (short) (i * 4 + 2));
                ib.put(i * 6 + 4, (short) (i * 4 + 3));
                ib.put(i * 6 + 3, (short) (i * 4));
            }

            ibo.upload(0, buffer);

            MemoryUtil.memFree(buffer);
        }
    }

    public void draw(CommandBuffer buffer) {
        buffer.vkCmdDrawIndexed(
                0, 0,
                0, 1,
                0, 12 + 12 + 12
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
        buffer.vkCmdDrawIndexed(
                0, 0,
                0, instances,
                0, 12 + 12 + 12
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
        CubePrimitive that = (CubePrimitive) o;
        return Float.compare(width, that.width) == 0 && Float.compare(height, that.height) == 0 && Float.compare(length, that.length) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, length);
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline, int i, int i1) {
        commandBuffer.draw(i, i1);
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline) {
        commandBuffer.draw(0, 12 + 12 + 12);
    }

    @Override
    public int size() {
        return 12 + 12 + 12;
    }

    @Override
    public void prepareCall(CommandBuffer commandBuffer) {
    }
}
