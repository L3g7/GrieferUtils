package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

public class SpecialValue {
	public static final double EPSILON = 10 * BinaryRelations.DEFAULT_COMPARISON_EPSILON;
	public double x;
	public double xFrom;
	public double xTo;
	public double fv;
	public double fvdeg;

	public SpecialValue(double x, double fv, double fvdeg) {
		this.x = x;
		this.fv = fv;
		this.fvdeg = fvdeg;
		xFrom = x - EPSILON;
		xTo = x + EPSILON;
	}
}
