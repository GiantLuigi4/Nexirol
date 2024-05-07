package tfc.nexirol.math;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class QuatHelper {
	public static Quaternionf fromMatrixInfo(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		float tr = m00 + m11 + m22;
		
		Quaternionf qf = new Quaternionf();
		if (tr > 0) {
			float S = (float) (Math.sqrt(tr + 1.0) * 2); // S=4*qw
			qf.w = 0.25f * S;
			qf.x = (m21 - m12) / S;
			qf.y = (m02 - m20) / S;
			qf.z = (m10 - m01) / S;
		} else if ((m00 > m11) & (m00 > m22)) {
			float S = (float) (Math.sqrt(1.0 + m00 - m11 - m22) * 2); // S=4*qx
			qf.w = (m21 - m12) / S;
			qf.x = 0.25f * S;
			qf.y = (m01 + m10) / S;
			qf.z = (m02 + m20) / S;
		} else if (m11 > m22) {
			float S = (float) (Math.sqrt(1.0 + m11 - m00 - m22) * 2); // S=4*qy
			qf.w = (m02 - m20) / S;
			qf.x = (m01 + m10) / S;
			qf.y = 0.25f * S;
			qf.z = (m12 + m21) / S;
		} else {
			float S = (float) (Math.sqrt(1.0 + m22 - m00 - m11) * 2); // S=4*qz
			qf.w = (m10 - m01) / S;
			qf.x = (m02 + m20) / S;
			qf.y = (m12 + m21) / S;
			qf.z = 0.25f * S;
		}
		
		return qf;
	}
	
	public static Quaternionf fromMatrix(Matrix4f matrix) {
		return fromMatrixInfo(matrix.m00(), matrix.m01(), matrix.m02(), matrix.m10(), matrix.m11(), matrix.m12(), matrix.m20(), matrix.m21(), matrix.m22());
	}
}
