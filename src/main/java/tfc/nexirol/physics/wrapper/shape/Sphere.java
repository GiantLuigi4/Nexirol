package tfc.nexirol.physics.wrapper.shape;

public class Sphere extends Shape {
    public final double radius;

    public Sphere(double radius) {
        super(EnumShapeType.SPHERE);
        this.radius = radius;
    }
}
