package tfc.test.noise.geom;

import org.lwjgl.system.MemoryUtil;
import tfc.renirol.frontend.enums.BufferUsage;
import tfc.renirol.frontend.enums.IndexSize;
import tfc.renirol.frontend.hardware.device.ReniLogicalDevice;
import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;
import tfc.renirol.frontend.reni.draw.batch.Drawable;
import tfc.renirol.frontend.reni.draw.instance.InstanceKey;
import tfc.renirol.frontend.reni.draw.instance.Instanceable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

public class QuadGrid implements Instanceable, Drawable, InstanceKey {
    private final GPUBuffer vbo;
    private final GPUBuffer ibo;
    final float width;
    final float height;
    final int primCount;

    public int putVec3(
            IntBuffer buffer, int start,
            float u, float v
    ) {
//        buffer.put(start, u);
//        buffer.put(start + 1, v);
        buffer.put(start, start);

        return start + 1;
    }

    public QuadGrid(
            ReniLogicalDevice device,
            float width, float height,
            int cellsX, int cellsY,
            float minU, float minV,
            float maxU, float maxV
    ) {
        this.width = width;
        this.height = height;
        primCount = 6 * cellsX * cellsY;

        vbo = new GPUBuffer(device, BufferUsage.VERTEX, (cellsX * 2 + cellsY * 2 + (cellsX * cellsY)) * 1 * 4);
        ibo = new GPUBuffer(device, BufferUsage.INDEX, primCount * 2);

        vbo.allocate();
        ibo.allocate();

        {
            ByteBuffer buffer = vbo.createByteBuf();
            IntBuffer fb = buffer.asIntBuffer();

            ByteBuffer buffer1 = ibo.createByteBuf();
            ShortBuffer ib = buffer1.asShortBuffer();

            int index = 0;
            // top/bottom
            for (int x = 0; x <= cellsX; x++) {
                for (int y = 0; y <= cellsY; y++) {
                    float pX = x / (float) cellsX;
                    float pY = y / (float) cellsY;

                    if (x != cellsX && y != cellsY) {
                        int id = (x * cellsX + y);
                        int idTo = id + (id / (cellsX ));

                        ib.put(id * 6 + 0, (short) (idTo + cellsX + 1));
                        ib.put(id * 6 + 1, (short) (idTo));
                        ib.put(id * 6 + 2, (short) (idTo + 1));

                        ib.put(id * 6 + 3, (short) (idTo + 1));
                        ib.put(id * 6 + 4, (short) (idTo + cellsX + 2));
                        ib.put(id * 6 + 5, (short) (idTo + cellsX + 1));
                    }

                    index = putVec3(
                            fb,
                            index,
                            pX * (maxU - minU) + minU,
                            pY * (maxV - minV) + minV
                    );
                }
            }
            vbo.upload(0, buffer);
            MemoryUtil.memFree(buffer);

            ibo.upload(0, buffer1);
            MemoryUtil.memFree(buffer1);
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
        QuadGrid that = (QuadGrid) o;
        return width == that.width && height == that.height && primCount == that.primCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, primCount);
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
