package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

public final class Units {
	public static final double PERC = 0.01;
	public static final double PROMIL = 0.001;
	public static final double YOTTA = 1.0E24;
	public static final double ZETTA = 1.0E21;
	public static final double EXA = 1.0E18;
	public static final double PETA = 1.0E15;
	public static final double TERA = 1.0E12;
	public static final double GIGA = 1000000000.0;
	public static final double MEGA = 1000000.0;
	public static final double KILO = 1000.0;
	public static final double HECTO = 100.0;
	public static final double DECA = 10.0;
	public static final double DECI = 0.1;
	public static final double CENTI = 0.01;
	public static final double MILLI = 0.001;
	public static final double MICRO = 0.000001;
	public static final double NANO = 0.000000001;
	public static final double PICO = 1.0E-12;
	public static final double FEMTO = 1.0E-15;
	public static final double ATTO = 1.0E-18;
	public static final double ZEPTO = 1.0E-21;
	public static final double YOCTO = 1.0E-24;

	public static final double METRE = 1.0;
	public static final double KILOMETRE = 1000.0 * METRE;
	public static final double CENTIMETRE = CENTI * METRE;
	public static final double MILLIMETRE = MILLI * METRE;
	public static final double INCH = 2.54 * CENTIMETRE;
	public static final double YARD = 0.9144 * METRE;
	public static final double FEET = 30.48 * CENTIMETRE;
	public static final double MILE = 1.609344 * KILOMETRE;
	public static final double NAUTICAL_MILE = 1.852 * KILOMETRE;

	public static final double METRE2 = METRE * METRE;
	public static final double CENTIMETRE2 = CENTIMETRE * CENTIMETRE;
	public static final double MILLIMETRE2 = MILLIMETRE * MILLIMETRE;
	public static final double ARE = (10.0 * METRE) * (10.0 * METRE);
	public static final double HECTARE = (100.0 * METRE) * (100.0 * METRE);
	public static final double KILOMETRE2 = KILOMETRE * KILOMETRE;
	public static final double ACRE = (66.0 * FEET) * (660.0 * FEET);

	public static final double MILLIMETRE3 = MILLIMETRE * MILLIMETRE * MILLIMETRE;
	public static final double CENTIMETRE3 = CENTIMETRE * CENTIMETRE * CENTIMETRE;
	public static final double METRE3 = METRE * METRE * METRE;
	public static final double KILOMETRE3 = KILOMETRE * KILOMETRE * KILOMETRE;
	public static final double MILLILITRE = CENTIMETRE3;
	public static final double LITRE = 1000.0 * MILLILITRE;
	public static final double GALLON = 3.78541178 * LITRE;
	public static final double PINT = 473.176473 * MILLILITRE;

	public static final double SECOND = 1.0;
	public static final double MILLISECOND = MILLI * SECOND;
	public static final double MINUTE = 60.0 * SECOND;
	public static final double HOUR = 60.0 * MINUTE;
	public static final double DAY = 24.0 * HOUR;
	public static final double WEEK = 7.0 * DAY;
	public static final double JULIAN_YEAR = 365.25 * DAY;

	public static final double KILOGRAM = 1.0;
	public static final double GRAM = 0.001 * KILOGRAM;
	public static final double MILLIGRAM = MILLI * GRAM;
	public static final double DECAGRAM = DECA * GRAM;
	public static final double TONNE = 1000.0 * KILOGRAM;
	public static final double OUNCE = 28.3495231 * GRAM;
	public static final double POUND = 0.45359237 * KILOGRAM;

	public static final double BIT = 1.0;
	public static final double KILOBIT = 1024.0 * BIT;
	public static final double MEGABIT = 1024.0 * KILOBIT;
	public static final double GIGABIT = 1024.0 * MEGABIT;
	public static final double TERABIT = 1024.0 * GIGABIT;
	public static final double PETABIT = 1024.0 * TERABIT;
	public static final double EXABIT = 1024.0 * PETABIT;
	public static final double ZETTABIT = 1024.0 * EXABIT;
	public static final double YOTTABIT = 1024.0 * ZETTABIT;
	public static final double BYTE = 8.0 * BIT;
	public static final double KILOBYTE = 1024.0 * BYTE;
	public static final double MEGABYTE = 1024.0 * KILOBYTE;
	public static final double GIGABYTE = 1024.0 * MEGABYTE;
	public static final double TERABYTE = 1024.0 * GIGABYTE;
	public static final double PETABYTE = 1024.0 * TERABYTE;
	public static final double EXABYTE = 1024.0 * PETABYTE;
	public static final double ZETTABYTE = 1024.0 * EXABYTE;
	public static final double YOTTABYTE = 1024.0 * ZETTABYTE;

	public static final double JOULE = (KILOGRAM * METRE * METRE);
	public static final double ELECTRONO_VOLT = 1.6021766208E-19 * JOULE;
	public static final double KILO_ELECTRONO_VOLT = KILO * ELECTRONO_VOLT;
	public static final double MEGA_ELECTRONO_VOLT = MEGA * ELECTRONO_VOLT;
	public static final double GIGA_ELECTRONO_VOLT = GIGA * ELECTRONO_VOLT;
	public static final double TERA_ELECTRONO_VOLT = TERA * ELECTRONO_VOLT;

	public static final double METRE_PER_SECOND = METRE / SECOND;
	public static final double KILOMETRE_PER_HOUR = KILOMETRE / HOUR;
	public static final double MILE_PER_HOUR = MILE / HOUR;
	public static final double KNOT = 0.514444444 * METRE / SECOND;

	public static final double METRE_PER_SECOND2 = METRE;
	public static final double KILOMETRE_PER_HOUR2 = KILOMETRE / (HOUR * HOUR);
	public static final double MILE_PER_HOUR2 = MILE / (HOUR * HOUR);

	public static final double RADIAN_ARC = 1.0;
	public static final double DEGREE_ARC = (MathConstants.PI / 180.0) * RADIAN_ARC;
	public static final double MINUTE_ARC = DEGREE_ARC / 60.0;
	public static final double SECOND_ARC = MINUTE_ARC / 60.0;
}