package tfc.nexirol.render.glsl;

import java.util.ArrayList;
import java.util.List;

public class ArrowOperatorProcessor extends PreProcessor {
	char[] breaks = new char[]{
			'.', '+', '-', '>', '<', ',', '\'', '"', '`',
			'|', ';', '=', '*', '&', '^', '%', '$', '#', '@', '!',
			'?', ':', '\\', '[', ']', '{', '}', '(', ')', '~',
	};
	
	// TODO: deal with block comments
	@Override
	public List<Line> transform(List<Line> input) {
		List<Line> output = new ArrayList<>();
		for (Line s : input) {
			if (s.contains("->")) {
				// TODO: deal with comments
				StringBuilder ifDef = new StringBuilder();
				ArrayList<String> defs = new ArrayList<>();
				
				StringBuilder text = new StringBuilder();
				StringBuilder text1 = new StringBuilder();
				boolean isDash = false;
				char[] chars = s.toCharArray();
				// TODO: grouping (multiple lines having similar -> operators being bunched into one ifdef statement)
				for (int i = 0; i < chars.length; i++) {
					char c = chars[i];
					
					if (c == '-') isDash = true;
					else if (c == '>' && isDash) {
						text.append('[');
						i += 1;
						loopChr:
						while (true) {
							for (char aBreak : breaks) {
								if (aBreak == chars[i]) break loopChr;
							}
							text.append(chars[i]);
							ifDef.append(chars[i]);
							i++;
						}
						defs.add(ifDef.toString());
						ifDef = new StringBuilder();
						i -= 1;
						text.append(']');
						isDash = false;
					} else {
						if (isDash) {
							text.append('-');
							text1.append('-');
						}
						isDash = false;
						text.append(c);
						text1.append(c);
					}
				}
				
				// TODO: deal with multiple ->'s on one line
				String def = defs.get(0);
				output.add(new Line(-1, "#ifdef " + def));
				output.add(new Line(s.ln, text.toString()));
				output.add(new Line(-1, "#else"));
				output.add(new Line(s.ln, text1.toString()));
				output.add(new Line(-1, "#endif"));
			} else {
				output.add(s);
			}
		}
		return output;
	}
}
