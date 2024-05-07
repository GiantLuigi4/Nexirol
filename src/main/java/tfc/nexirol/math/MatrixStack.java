package tfc.nexirol.math;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayDeque;

public class MatrixStack {
	public static class Entry {
		Matrix4f matr;
		Matrix3f norm;
		
		public Entry(Matrix4f matr, Matrix3f norm) {
			this.matr = matr;
			this.norm = norm;
		}
		
		public Entry() {
			matr = new Matrix4f();
			norm = new Matrix3f();
		}
		
		public Entry(Entry matrix4f) {
			this(matrix4f.matr, matrix4f.norm);
		}
		
		Entry identity() {
			matr.identity();
			norm.identity();
			return this;
		}
		
		void rotateXYZ(float x, float y, float z) {
			matr.rotateXYZ(x, y, z);
			norm.rotateXYZ(x, y, z);
		}
		
		public void translate(float x, float y, float z) {
			matr.translate(x, y, z);
		}
		
		public void scale(float x, float y, float z) {
			matr.scale(x, y, z);
		}
	}
	
	ArrayDeque<Entry> stack = new ArrayDeque<>();
	Entry matrix4f;
	
	public MatrixStack() {
		matrix4f = new Entry().identity();
	}
	
	public MatrixStack(Matrix4f matrix4f, Matrix3f norm) {
		this.matrix4f = new Entry(matrix4f, norm);
	}
	
	public void translate(double x, double y, double z) {
		matrix4f.translate((float) x, (float) y, (float) z);
	}
	
	public void rotate(double x, double y, double z) {
		matrix4f.rotateXYZ(
				(float) Math.toRadians(x),
				(float) Math.toRadians(y),
				(float) Math.toRadians(z)
		);
	}
	
	public void scale(double x, double y, double z) {
		matrix4f.scale((float) x, (float) y, (float) z);
	}
	
	public Matrix4f transformation() {
		return matrix4f.matr;
	}
	
	public Matrix3f normal() {
		return matrix4f.norm;
	}
	
	public void push() {
		stack.push(matrix4f);
		matrix4f = new Entry(matrix4f);
	}
	
	public void pop() {
		matrix4f = stack.pop();
	}
}
