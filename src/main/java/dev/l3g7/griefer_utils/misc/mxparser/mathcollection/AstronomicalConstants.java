package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

public final class AstronomicalConstants {
	public static final double LIGHT_YEAR = PhysicalConstants.LIGHT_SPEED * Units.JULIAN_YEAR;
	public static final double ASTRONOMICAL_UNIT = 149597870700.0 * Units.METRE;
	public static final double PARSEC = 206264.806247096 * ASTRONOMICAL_UNIT;
	public static final double KILOPARSEC = Units.KILO * PARSEC;
	public static final double EARTH_RADIUS_EQUATORIAL = 6378.1370 * Units.KILOMETRE;
	public static final double EARTH_RADIUS_POLAR = 6356.7523 * Units.KILOMETRE;
	public static final double EARTH_RADIUS_MEAN = 6371.0088 * Units.KILOMETRE;
	public static final double EARTH_MASS = 5.9722 * Units.YOTTA * Units.KILOGRAM;
	public static final double EARTH_SEMI_MAJOR_AXIS = 1.000001018 * ASTRONOMICAL_UNIT;
	public static final double MOON_RADIUS_MEAN = 1737.1 * Units.KILOMETRE;
	public static final double MOON_MASS = 0.012300037 * EARTH_MASS;
	public static final double MONN_SEMI_MAJOR_AXIS = 384399 * Units.KILOMETRE;
	public static final double SOLAR_RADIUS = 695700 * Units.KILOMETRE;
	public static final double SOLAR_MASS = 332946.0487 * EARTH_MASS;
	public static final double MERCURY_RADIUS_MEAN = 2439.7 * Units.KILOMETRE;
	public static final double MERCURY_MASS = 0.0553 * EARTH_MASS;
	public static final double MERCURY_SEMI_MAJOR_AXIS = 0.387098 * ASTRONOMICAL_UNIT;
	public static final double VENUS_RADIUS_MEAN = 6051.8 * Units.KILOMETRE;
	public static final double VENUS_MASS = 0.815 * EARTH_MASS;
	public static final double VENUS_SEMI_MAJOR_AXIS = 0.723332 * ASTRONOMICAL_UNIT;
	public static final double MARS_RADIUS_MEAN = 3389.5 * Units.KILOMETRE;
	public static final double MARS_MASS = 0.107 * EARTH_MASS;
	public static final double MARS_SEMI_MAJOR_AXIS = 1.523679 * ASTRONOMICAL_UNIT;
	public static final double JUPITER_RADIUS_MEAN = 69911 * Units.KILOMETRE;
	public static final double JUPITER_MASS = 317.8 * EARTH_MASS;
	public static final double JUPITER_SEMI_MAJOR_AXIS = 5.20260 * ASTRONOMICAL_UNIT;
	public static final double SATURN_RADIUS_MEAN = 58232 * Units.KILOMETRE;
	public static final double SATURN_MASS = 95.159 * EARTH_MASS;
	public static final double SATURN_SEMI_MAJOR_AXIS = 9.5549 * ASTRONOMICAL_UNIT;
	public static final double URANUS_RADIUS_MEAN = 25362 * Units.KILOMETRE;
	public static final double URANUS_MASS = 14.536 * EARTH_MASS;
	public static final double URANUS_SEMI_MAJOR_AXIS = 19.2184 * ASTRONOMICAL_UNIT;
	public static final double NEPTUNE_RADIUS_MEAN = 24622 * Units.KILOMETRE;
	public static final double NEPTUNE_MASS = 17.147 * EARTH_MASS;
	public static final double NEPTUNE_SEMI_MAJOR_AXIS = 30.110387 * ASTRONOMICAL_UNIT;
}