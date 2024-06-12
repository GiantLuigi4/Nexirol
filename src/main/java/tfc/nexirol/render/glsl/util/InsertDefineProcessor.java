package tfc.nexirol.render.glsl.util;

import tfc.nexirol.render.glsl.Line;
import tfc.nexirol.render.glsl.PreProcessor;

import java.util.ArrayList;
import java.util.List;

public class InsertDefineProcessor extends PreProcessor {
	String define;

	public InsertDefineProcessor(String define) {
		this.define = define;
	}
	
	@Override
	public List<Line> transform(List<Line> input) {
		List<Line> output = new ArrayList<>();
		for (Line s : input) {
			if (s.trim().startsWith("#version")) {
				output.add(s);
				output.add(new Line(-1, "#define " + define));
			} else output.add(s);
		}
		return output;
	}
}
