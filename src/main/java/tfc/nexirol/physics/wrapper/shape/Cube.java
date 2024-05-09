package tfc.nexirol.physics.wrapper.shape;

public class Cube extends Shape {
    public final double width, height, length;

    public Cube(double width, double height, double length) {
        super(EnumShapeType.CUBE);
        this.width = width;
        this.height = height;
        this.length = length;
    }
}
