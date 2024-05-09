package tfc.nexirol.physics.wrapper;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import tfc.nexirol.physics.physx.PhysXWorld;
import tfc.nexirol.physics.wrapper.shape.Shape;

import java.util.HashMap;
import java.util.function.Consumer;

public class RigidBody {
    public final boolean isStatic;
    public final Shape collider;
    public final Material material;

    public RigidBody(boolean isStatic, Shape collider, Material material) {
        this.isStatic = isStatic;
        this.collider = collider;
        this.material = material;
    }

    HashMap<PhysicsWorld, Runnable> removeFuncs = new HashMap<>();

    public void bindRemove(PhysicsWorld world, Runnable o) {
        removeFuncs.put(world, o);
    }

    public final Vector3f vec = new Vector3f();

    public RigidBody setPosition(float x, float y, float z) {
        vec.set(x, y, z);
        return this;
    }

    public final Quaternionf quat = new Quaternionf(0, 0, 0, 1);

    public RigidBody setOrientation(float x, float y, float z, float w) {
        quat.set(x, y, z, w);
        return this;
    }

    Consumer<RigidBody> onUpdate = (body) -> {};

    public void update() {
        onUpdate.accept(this);
    }

    public RigidBody onUpdate(Consumer<RigidBody> consumer) {
        this.onUpdate = consumer;
        return this;
    }
}
