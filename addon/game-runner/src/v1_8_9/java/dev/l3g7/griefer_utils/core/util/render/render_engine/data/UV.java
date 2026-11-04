package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

public record UV(double minU, double maxU, double minV, double maxV) {

	public double width() {
		return maxU - minU;
	}

	public double height() {
		return maxV - minV;
	}

}
