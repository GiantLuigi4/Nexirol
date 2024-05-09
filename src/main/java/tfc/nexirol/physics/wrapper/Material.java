package tfc.nexirol.physics.wrapper;

public class Material {
    public final float staticFriction;
    public final float dynamicFriction;
    public final float restitution;

    public Material(float staticFriction, float dynamicFriction, float restitution) {
        this.staticFriction = staticFriction;
        this.dynamicFriction = dynamicFriction;
        this.restitution = restitution;
    }
}
