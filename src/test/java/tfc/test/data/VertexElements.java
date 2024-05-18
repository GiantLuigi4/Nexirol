package tfc.test.data;

import tfc.renirol.frontend.enums.prims.NumericPrimitive;
import tfc.renirol.frontend.rendering.resource.buffer.DataElement;

public class VertexElements {
    public static final DataElement POSITION_XYZW = new DataElement(NumericPrimitive.FLOAT, 4);
    public static final DataElement POSITION_XYZ = new DataElement(NumericPrimitive.FLOAT, 3);
    public static final DataElement COLOR_RGBA = new DataElement(NumericPrimitive.FLOAT, 4);
    public static final DataElement COLOR_RGB = new DataElement(NumericPrimitive.FLOAT, 3);
    public static final DataElement NORMAL_XYZ = new DataElement(NumericPrimitive.FLOAT, 3);

    public static final DataElement UV0 = new DataElement(NumericPrimitive.FLOAT, 2);
    public static final DataElement UV1 = new DataElement(NumericPrimitive.FLOAT, 2);
    public static final DataElement UV2 = new DataElement(NumericPrimitive.FLOAT, 2);
    public static final DataElement INDEX16 = new DataElement(NumericPrimitive.INT, 1);
    public static final DataElement INDEX32 = new DataElement(NumericPrimitive.LONG, 1);
    public static final DataElement ID = new DataElement(NumericPrimitive.INT, 1);
}
