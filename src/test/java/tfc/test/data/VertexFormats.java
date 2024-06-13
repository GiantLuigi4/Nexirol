package tfc.test.data;

import tfc.renirol.frontend.rendering.resource.buffer.DataFormat;

public class VertexFormats {
    public static final DataFormat POS4 = new DataFormat(VertexElements.POSITION_XYZW);
    public static final DataFormat POS4_NORMAL3 = new DataFormat(VertexElements.POSITION_XYZW, VertexElements.NORMAL_XYZ);
    public static final DataFormat POS4_COLOR4 = new DataFormat(VertexElements.POSITION_XYZW, VertexElements.COLOR_RGBA);
    public static final DataFormat POS4_COLOR4_UV2 = new DataFormat(VertexElements.POSITION_XYZW, VertexElements.COLOR_RGBA, VertexElements.UV0);
    public static final DataFormat POS4_UV2 = new DataFormat(VertexElements.POSITION_XYZW, VertexElements.UV0);
    public static final DataFormat INDEX16 = new DataFormat(VertexElements.INDEX16);
    public static final DataFormat INDEX32 = new DataFormat(VertexElements.INDEX32);
    public static final DataFormat UV2 = new DataFormat(VertexElements.UV0);
}
