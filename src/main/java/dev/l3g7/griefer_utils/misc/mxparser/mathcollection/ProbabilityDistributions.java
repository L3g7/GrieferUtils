package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

import java.util.Random;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;

public final class ProbabilityDistributions {
	public static Random randomGenerator = new Random();
	public static double rndUniformContinuous(double a, double b, Random rnd) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (b < a) return Double.NaN;
		if (a == b) return a;
		return a + rnd.nextDouble() * (b - a);
	}
	public static double rndUniformContinuous(Random rnd) {
		return rnd.nextDouble();
	}
	public static double pdfUniformContinuous(double x, double a, double b) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (b < a) return Double.NaN;
		if (a == b) {
			if (x == a) return 1;
			else return 0;
		}
		if ( (x < a) || (x > b) ) return 0;
		if (x == Double.NEGATIVE_INFINITY) return 0.0;
		if (x == Double.POSITIVE_INFINITY) return 0.0;
		return 1.0 / (b - a);
	}
	public static double cdfUniformContinuous(double x, double a, double b) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (b < a) return Double.NaN;
		if (a == b) {
			if (x < a) return 0.0;
			else return 1.0;
		}
		if (x < a) return 0.0;
		if (x >= b) return 1.0;
		if (x == Double.NEGATIVE_INFINITY) return 0.0;
		return (x - a) / (b - a);
	}
	public static double qntUniformContinuous(double q, double a, double b) {
		if (Double.isNaN(q)) return Double.NaN;
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if ( (q < 0.0) || (q > 1.0) ) return Double.NaN;
		if (b < a) return Double.NaN;
		if (a == b) {
			if (q == 1.0) return b;
			else return Double.NaN;
		}
		if (q == 0.0) return a;
		if (q == 1.0) return b;
		return a + q*(b-a);
	}
	public static double rndInteger(int a, int b, Random rnd) {
		if (b < a) return Double.NaN;
		if (a == b) return a;
		int n = (b - a) + 1;
		return a + rnd.nextInt(n);
	}

	public static int rndInteger(Random rnd) {
		return rnd.nextInt();
	}
	public static int rndIndex(int n, Random rnd) {
		if (n < 0) return -1;
		return rnd.nextInt(n);
	}
	public static double rndNormal(double mean, double stddev, Random rnd) {
		if (Double.isNaN(mean)) return Double.NaN;
		if (Double.isNaN(stddev)) return Double.NaN;
		if (rnd == null) return Double.NaN;
		if (stddev < 0) return Double.NaN;
		if (stddev == 0) return mean;
		double x, a, v1;
		double b, v2;
		double r, fac;
		boolean polarTransform;
		do {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			a = rnd.nextDouble();
			b = rnd.nextDouble();
			v1 = 2.0*a - 1.0;
			v2 = 2.0*b - 1.0;
			r = (v1*v1) + (v2*v2);
			if (r >= 1.0 || r == 0.0) {
				x = 0.0;
				polarTransform = false;
			} else {
				fac = MathFunctions.sqrt( -2.0 * MathFunctions.ln(r) / r);
				x = v1*fac;
				polarTransform = true;
			}
		} while (!polarTransform);
		return mean + (stddev*x);
	}
	public static double pdfNormal(double x, double mean, double stddev) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(mean)) return Double.NaN;
		if (Double.isNaN(stddev)) return Double.NaN;
		if (stddev < 0) return Double.NaN;
		if (stddev == 0) {
			if (x == mean) return 1.0;
			else return 0;
		}
		if (x == Double.NEGATIVE_INFINITY) return 0.0;
		if (x == Double.POSITIVE_INFINITY) return 0.0;
		double d = (x - mean) / stddev;
		return MathFunctions.exp( -0.5*d*d ) / ( MathConstants.SQRT2Pi*stddev );
	}
	public static double cdfNormal(double x, double mean, double stddev) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(mean)) return Double.NaN;
		if (Double.isNaN(stddev)) return Double.NaN;
		if (stddev < 0) return Double.NaN;
		if (stddev == 0) {
			if (x < mean) return 0.0;
			else return 1.0;
		}
		if (x == Double.NEGATIVE_INFINITY) return 0.0;
		if (x == Double.POSITIVE_INFINITY) return 1.0;
		return 0.5 * SpecialFunctions.erfc( (mean - x) / (stddev * MathConstants.SQRT2));
	}
	public static double qntNormal(double q, double mean, double stddev) {
		if (Double.isNaN(q)) return Double.NaN;
		if (Double.isNaN(mean)) return Double.NaN;
		if (Double.isNaN(stddev)) return Double.NaN;
		if ( (q < 0.0) || (q > 1.0) ) return Double.NaN;
		if (stddev < 0) return Double.NaN;
		if (stddev == 0) {
			if (q == 1.0) return mean;
			else return Double.NaN;
		}
		if (q == 0.0) return Double.NEGATIVE_INFINITY;
		if (q == 1.0) return Double.POSITIVE_INFINITY;
		return mean - ( stddev * MathConstants.SQRT2 * SpecialFunctions.erfcInv( 2.0*q ) );
	}
}
