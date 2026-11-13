package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.function.Function;

/**
 * Clockwise vertices of a rendered quad.<br>
 * 0, 0, 0 is the middle of a block to simplify rotating.
 */
public record Quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {

	/**
	 * Creates new vertices on the y plane, centered at the middle of a block.
	 */
	public static Quad onYPlane(double xSize, double zSize) {
		double halfXSize = xSize / 2d;
		double halfZSize = zSize / 2d;

		return new Quad(
			new Vec3(-halfXSize, 0, -halfZSize),
			new Vec3(halfXSize, 0, -halfZSize),
			new Vec3(halfXSize, 0, halfZSize),
			new Vec3(-halfXSize, 0, halfZSize)
		);
	}

	/**
	 * Creates new vertices on the x plane, centered at the middle of a block.
	 */
	public static Quad onXPlane(double ySize, double zSize) {
		double halfYSize = ySize / 2d;
		double halfZSize = zSize / 2d;

		return new Quad(
			new Vec3(0, -halfYSize, halfZSize),
			new Vec3(0, -halfYSize, -halfZSize),
			new Vec3(0, halfYSize, -halfZSize),
			new Vec3(0, halfYSize, halfZSize)
		);
	}

	/**
	 * Rotates all vertices around the origin.
	 * The rotation is clockwise when looking along the +x-axis.
	 */
	public Quad rotateX(double byDegrees) {
		double cos = Math.cos(Math.toRadians(-byDegrees));
		double sin = Math.sin(Math.toRadians(-byDegrees));

		return transform(v -> {
			return new Vec3(
				v.xCoord,
				v.yCoord * cos + v.zCoord * sin,
				v.zCoord * cos - v.yCoord * sin
			);
		});
	}

	/**
	 * Rotates all vertices around the origin.
	 * The rotation is clockwise when looking along the +y-axis.
	 */
	public Quad rotateY(double byDegrees) {
		double cos = Math.cos(Math.toRadians(byDegrees));
		double sin = Math.sin(Math.toRadians(byDegrees));

		return transform(v -> {
			return new Vec3(
				v.xCoord * cos + v.zCoord * sin,
				v.yCoord,
				v.zCoord * cos - v.xCoord * sin
			);
		});
	}

	/**
	 * Rotates all vertices around the origin.
	 * The rotation is clockwise when looking along the +z-axis.
	 */
	public Quad rotateZ(double byDegrees) {
		double cos = Math.cos(Math.toRadians(byDegrees));
		double sin = Math.sin(Math.toRadians(byDegrees));

		return transform(v -> {
			return new Vec3(
				v.xCoord * cos - v.yCoord * sin,
				v.yCoord * cos + v.xCoord * sin,
				v.zCoord
			);
		});
	}

	public Quad translate(double deltaX, double deltaY, double deltaZ) {
		return transform(v -> new Vec3(v.xCoord + deltaX, v.yCoord + deltaY, v.zCoord + deltaZ));
	}

	public Quad translate(Vec3i delta) {
		return translate(delta.getX(), delta.getY(), delta.getZ());
	}

	private Quad transform(Function<Vec3, Vec3> transformer) {
		return new Quad(
			transformer.apply(a),
			transformer.apply(b),
			transformer.apply(c),
			transformer.apply(d)
		);
	}

}
