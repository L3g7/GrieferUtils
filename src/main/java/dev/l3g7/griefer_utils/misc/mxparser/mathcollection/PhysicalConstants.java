package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

public final class PhysicalConstants {
	public static final double LIGHT_SPEED = 299792458.0 * Units.METRE_PER_SECOND;
	public static final double GRAVITATIONAL_CONSTANT = 6.67408E-11 * Units.METRE3 * (1.0 / Units.KILOGRAM) * (1.0) ;
	public static final double GRAVIT_ACC_EARTH = 9.80665 * Units.METRE_PER_SECOND2;
	public static final double PLANCK_CONSTANT = 6.626070040E-34 * Units.METRE2 * Units.KILOGRAM / Units.SECOND;
	public static final double PLANCK_CONSTANT_REDUCED = PLANCK_CONSTANT / (2 * MathConstants.PI);
	public static final double PLANCK_LENGTH = 1.616229E-35 * Units.METRE;
	public static final double PLANCK_MASS = 2.176470E-8 * Units.KILOGRAM;
	public static final double PLANCK_TIME = 5.39116E-44 * Units.SECOND;
}