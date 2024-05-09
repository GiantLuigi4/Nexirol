package tfc.nexirol.physics.wrapper;

public abstract class PhysicsWorld {
    public abstract void addBody(RigidBody body);
    public abstract void tick();
}
