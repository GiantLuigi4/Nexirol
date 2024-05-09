package tfc.nexirol.physics.wrapper;

import tfc.renirol.frontend.rendering.command.CommandBuffer;
import tfc.renirol.frontend.rendering.command.pipeline.GraphicsPipeline;
import tfc.renirol.frontend.reni.draw.batch.Drawable;
import tfc.renirol.frontend.reni.draw.instance.InstanceKey;
import tfc.renirol.frontend.reni.draw.instance.Instanceable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PhysicsDrawable implements Drawable, InstanceKey, Instanceable {
    Drawable drawable;
    Instanceable graphics;
    InstanceKey key;
    BiConsumer<CommandBuffer, Integer> setup;
    Consumer<CommandBuffer> prepare;
    RigidBody body;

    public PhysicsDrawable(Instanceable graphics, BiConsumer<CommandBuffer, Integer> setup, Consumer<CommandBuffer> prepare, RigidBody body) {
        if (graphics instanceof Drawable drawable) this.drawable = drawable;
        this.graphics = graphics;
        key = graphics.comparator();
        this.setup = setup;
        this.prepare = prepare;
        this.body = body;
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline, int i, int i1) {
        drawable.draw(commandBuffer, graphicsPipeline, i, i1);
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline) {
        drawable.draw(commandBuffer, graphicsPipeline);
    }

    @Override
    public int size() {
        return drawable.size();
    }

    @Override
    public void bind(CommandBuffer commandBuffer) {
        key.bind(commandBuffer);
    }

    @Override
    public void draw(CommandBuffer commandBuffer, GraphicsPipeline graphicsPipeline, int i) {
        key.draw(commandBuffer, graphicsPipeline, i);
    }

    @Override
    public InstanceKey comparator() {
        return key;
    }

    @Override
    public void setup(CommandBuffer commandBuffer, int i) {
        setup.accept(commandBuffer, i);
    }

    @Override
    public void prepareCall(CommandBuffer commandBuffer) {
        prepare.accept(commandBuffer);
    }
}
