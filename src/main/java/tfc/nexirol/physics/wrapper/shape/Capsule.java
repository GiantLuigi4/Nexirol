package tfc.nexirol.physics.wrapper.shape;

public class Capsule extends Shape {
    public final double radius, height;

    public Capsule(double radius, float height) {
        super(EnumShapeType.CAPSULE);
        this.radius = radius;
        this.height = height;
    }
}
