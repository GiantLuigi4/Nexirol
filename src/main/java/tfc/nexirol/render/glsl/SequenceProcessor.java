package tfc.nexirol.render.glsl;

import java.util.List;

public class SequenceProcessor extends PreProcessor{
    PreProcessor[] preProcessors;

    public SequenceProcessor(PreProcessor... preProcessors) {
        this.preProcessors = preProcessors;
    }

    @Override
    public List<Line> transform(List<Line> input) {
        for (PreProcessor preProcessor : preProcessors)
            input = preProcessor.transform(input);
        return input;
    }
}
