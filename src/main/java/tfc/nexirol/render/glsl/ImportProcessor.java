package tfc.nexirol.render.glsl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ImportProcessor extends PreProcessor {
	Function<String, InputStream> streamGen;
	
	public ImportProcessor(Function<String, InputStream> streamGen) {
		this.streamGen = streamGen;
	}
	
	@Override
	public List<Line> transform(List<Line> input) {
		List<Line> output = new ArrayList<>();
		for (Line line : input) {
			if (line.trim().startsWith("#include")) {
				String text = line.trim().substring("#include".length()).trim();
				
				if (!(text.startsWith("<") && text.endsWith(">")))
					// TODO: tell file name and line #
					throw new RuntimeException("Include statements must follow the C-Style: #include <file/to/include.ext>");
				text = text.substring(1, text.length() - 1).trim();
				
				InputStream stream = streamGen.apply(text);
				try {
					byte[] bytes = stream.readAllBytes();
					
					output.addAll(PreProcessor.toLines(Arrays.asList(new String(bytes).split("\n"))));
				} catch (Throwable ignored) {
				}
			} else {
				output.add(line);
			}
		}
		return output;
	}
}
