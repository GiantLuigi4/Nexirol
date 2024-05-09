package tfc.nexirol.render.glsl;

import java.util.ArrayList;
import java.util.List;

public class MultiProcessor extends PreProcessor {
	boolean instancing, multidraw;
	
	public MultiProcessor(boolean instancing, boolean multidraw) {
		this.instancing = instancing;
		this.multidraw = multidraw;
	}
	
	@Override
	public List<Line> transform(List<Line> input) {
		if (!instancing && !multidraw) return input;
		
		// TODO: deal with line numbers
		List<Line> output = new ArrayList<>();
		for (Line s : input) {
			if (s.trim().startsWith("#version")) {
				output.add(s);
				
				if (instancing && multidraw)
					output.add(new Line(-1, "#define MultiID (gl_InstanceIndex + gl_DrawID)"));
				else
					output.add(new Line(-1, "#define MultiID " + (instancing ? "gl_InstanceIndex" : "gl_DrawID")));
				
				if (instancing) output.add(new Line(-1, "#define INSTANCING"));
				if (instancing && multidraw) output.add(new Line(-1, "#define MULTI_INSTANCE"));
				if (instancing || multidraw) output.add(new Line(-1, "#define MULTI"));
			} else output.add(s);
		}
		return output;
	}
}
