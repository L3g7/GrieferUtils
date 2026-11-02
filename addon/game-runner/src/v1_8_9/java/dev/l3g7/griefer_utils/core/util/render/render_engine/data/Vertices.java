package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import net.minecraft.util.Vec3;

import java.util.function.Function;

public record Vertices(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {

	public Vertices rotateYaw(float by) {
		return transform(v -> v.rotateYaw(by));
	}

	public Vertices translate(double deltaX, double deltaY, double deltaZ) {
		return transform(v -> new Vec3(v.xCoord + deltaX, v.yCoord + deltaY, v.zCoord + deltaZ));
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
