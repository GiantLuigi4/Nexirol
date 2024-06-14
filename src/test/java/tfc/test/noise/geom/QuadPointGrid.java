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
import java.nio.ShortBuffer;
import java.util.Objects;

public class QuadPointGrid implements Instanceable, Drawable, InstanceKey {
    private final GPUBuffer ibo;
    final float width;
    final float height;
    final int primCount;

    public QuadPointGrid(
            ReniLogicalDevice device,
            float width, float height,
            int cellsX, int cellsY,
            boolean tris
    ) {
        this.width = width;
        this.height = height;
        primCount = (tris ? 6 : 4) * cellsX * cellsY;

        ibo = new GPUBuffer(device, BufferUsage.INDEX, primCount * 2);

        ibo.allocate();

        {
            ByteBuffer buffer1 = ibo.createByteBuf();
            ShortBuffer ib = buffer1.asShortBuffer();

            int index = 0;
            // top/bottom
            for (int x = 0; x <= cellsX; x++) {
                for (int y = 0; y <= cellsY; y++) {
                    if (x != cellsX && y != cellsY) {
                        int id = (x * cellsX + y);
                        int idTo = id + (id / (cellsX ));

                        if (tris) {
                            ib.put(id * 6 + 0, (short) (idTo + cellsX + 1));
                            ib.put(id * 6 + 1, (short) (idTo));
                            ib.put(id * 6 + 2, (short) (idTo + 1));

                            ib.put(id * 6 + 3, (short) (idTo + 1));
                            ib.put(id * 6 + 4, (short) (idTo + cellsX + 2));
                            ib.put(id * 6 + 5, (short) (idTo + cellsX + 1));
                        } else {
                            ib.put(id * 4 + 0, (short) (idTo + cellsX + 2));
                            ib.put(id * 4 + 1, (short) (idTo + cellsX + 1));
                            ib.put(id * 4 + 2, (short) (idTo + 1));
                            ib.put(id * 4 + 3, (short) (idTo));
                        }
                    }
                }
            }

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
        ibo.destroy();
    }

    @Override
    public void bind(CommandBuffer buffer) {
        buffer.bindIbo(IndexSize.INDEX_16, ibo);
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
        QuadPointGrid that = (QuadPointGrid) o;
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
