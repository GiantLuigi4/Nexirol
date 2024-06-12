package tfc.nexirol.render.glsl.util;

import tfc.nexirol.render.glsl.Line;
import tfc.nexirol.render.glsl.PreProcessor;

import java.util.List;

public final class NoOpProcessor extends PreProcessor {
    public static final NoOpProcessor INSTANCE = new NoOpProcessor();

    private NoOpProcessor() {
    }

    @Override
    public List<Line> transform(List<Line> input) {
        return input;
    }
}
