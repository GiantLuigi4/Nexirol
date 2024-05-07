package tfc.nexirol.render.glsl;

public class Line {
	public final int ln;
	public final String text;
	
	public Line(int ln, String text) {
		this.ln = ln;
		this.text = text;
	}
	
	public boolean startsWith(String s) {
		return text.startsWith(s);
	}
	
	public char[] toCharArray() {
		return text.toCharArray();
	}
	
	public boolean contains(String s) {
		return text.contains(s);
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public String trim() {
		return text.trim();
	}
	
	public String replace(String src, String dst) {
		return text.replace(src, dst);
	}
	
	public String substring(int start) {
		return text.substring(start);
	}
}