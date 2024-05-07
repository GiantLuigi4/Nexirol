package tfc.nexirol.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class VecMath {
	public static Vector3f lerp(double pct, Vector3f start, Vector3f end) {
		return new Vector3f(start).mul((float) pct).add(end.x * (float) (1 - pct), end.y * (float) (1 - pct), end.z * (float) (1 - pct));
	}
	
	public static Vector3f traceVector(Vector3f pixel, Matrix4f projection, Matrix4f modelView) {
		Vector4f coord = new Vector4f(pixel.x, pixel.y, 0, 1f);
		new Matrix4f(projection).invert().transform(coord);
		coord = new Matrix4f(modelView).invert().transform(coord);
		pixel.x = 0;
		pixel.y = 0;
		pixel.z = 0;
		modelView.getTranslation(pixel);
		coord.sub(pixel.x, pixel.y, pixel.z, 0);
		coord.w = 0;
		coord.normalize();

		pixel.x = coord.x;
		pixel.y = coord.y;
		pixel.z = coord.z;

		return pixel;
	}
}
