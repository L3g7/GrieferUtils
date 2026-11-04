package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.function.Function;

public record Vertices(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {

	/**
	 * Creates new vertices on the y plane, centered at the y-axis.
	 */
	public static Vertices onYPlane(double width, double height) {
		double halfWidth = width / 2d;
		double halfHeight = height / 2d;

		return new Vertices(
			new Vec3(-halfWidth, 0, -halfHeight),
			new Vec3(halfWidth, 0, -halfHeight),
			new Vec3(halfWidth, 0, halfHeight),
			new Vec3(-halfWidth, 0, halfHeight)
		);
	}

	public Vertices rotateY(float by) {
		return transform(v -> v.rotateYaw(by));
	}

	public Vertices translate(double deltaX, double deltaY, double deltaZ) {
		return transform(v -> new Vec3(v.xCoord + deltaX, v.yCoord + deltaY, v.zCoord + deltaZ));
	}

	public Vertices translate(Vec3i delta) {
		return translate(delta.getX(), delta.getY(), delta.getZ());
	}

	private Vertices transform(Function<Vec3, Vec3> transformer) {
		return new Vertices(
			transformer.apply(a),
			transformer.apply(b),
			transformer.apply(c),
			transformer.apply(d)
		);
	}

}
