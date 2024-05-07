package tfc.nexirol.math;

import org.joml.Matrix4f;

public class Matrices {
	public static Matrix4f projection(float fov, double x, double y, float near, float far) {
		return new Matrix4f().perspective(fov, (float) (x / y), near, far);
	}
	
	public static Matrix4f orientation(float lookX, float lookY, float lookZ) {
		return new Matrix4f().setLookAt(
				0.0f, 0.0f, 0.0f,
				lookX, lookY, lookZ,
				0.0f, 1.0f, 0.0f
		);
	}
}
