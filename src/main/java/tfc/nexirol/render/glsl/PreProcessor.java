package tfc.nexirol.render.glsl;

import java.util.ArrayList;
import java.util.List;

public abstract class PreProcessor {
	public static List<Line> toLines(List<String> src) {
		List<Line> out = new ArrayList<>();
		// TODO: deal with comments
		for (int i = 0; i < src.size(); i++) out.add(new Line(i, src.get(i)));
		return out;
	}
	
	public static String toText(List<Line> lns) {
		StringBuilder builder = new StringBuilder();
		Line prev = null;
		for (Line ln : lns) {
			if (prev != null && prev.ln != (ln.ln - 1)) {
//				if (ln.ln != -1)
//					builder.append("#line ").append(ln.ln + 1).append("\n");
			}
			builder.append(ln.text.replace("\n", "")).append("\n");
			prev = ln;
		}
		return builder.toString();
	}
	
	public abstract List<Line> transform(List<Line> input);
}