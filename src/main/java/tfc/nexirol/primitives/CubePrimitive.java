package tfc.nexirol.primitives;

import tfc.renirol.frontend.rendering.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;
import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;
import tfc.renirol.frontend.rendering.resource.buffer.GPUBuffer;

public class CubePrimitive {
//    private final GPUBuffer cube;

    final float width;
    final float height;
    final float length;

    /**
     * @param format must be suitable for POSITION, COLOR, NORMAL
     * @param width  the width of the box
     * @param height the height of the box
     * @param length the length of the box
     */
    public CubePrimitive(DataFormat format, float width, float height, float length) {
        this.width = width;
        this.height = height;
        this.length = length;


    }
}
