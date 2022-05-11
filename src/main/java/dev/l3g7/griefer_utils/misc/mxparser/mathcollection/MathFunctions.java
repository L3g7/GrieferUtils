package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;

public final class MathFunctions {
	public static double plus(double a, double b) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (!mXparser.checkIfCanonicalRounding()) return a + b;
		if (Double.isInfinite(a)) return a + b;
		if (Double.isInfinite(b)) return a + b;
		BigDecimal da = BigDecimal.valueOf(a);
		BigDecimal db = BigDecimal.valueOf(b);
		return da.add(db).doubleValue();
	}
	public static double minus(double a, double b) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (!mXparser.checkIfCanonicalRounding()) return a - b;
		if (Double.isInfinite(a)) return a - b;
		if (Double.isInfinite(b)) return a - b;
		BigDecimal da = BigDecimal.valueOf(a);
		BigDecimal db = BigDecimal.valueOf(b);
		return da.subtract(db).doubleValue();
	}
	public static double multiply(double a, double b) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (!mXparser.checkIfCanonicalRounding()) return a * b;
		if (Double.isInfinite(a)) return a * b;
		if (Double.isInfinite(b)) return a * b;
		BigDecimal da = BigDecimal.valueOf(a);
		BigDecimal db = BigDecimal.valueOf(b);
		return da.multiply(db).doubleValue();
	}
	public static double div(double a, double b) {
		if (b == 0) return Double.NaN;
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (!mXparser.checkIfCanonicalRounding()) return a / b;
		if (Double.isInfinite(a)) return a / b;
		if (Double.isInfinite(b)) return a / b;
		BigDecimal da = BigDecimal.valueOf(a);
		BigDecimal db = BigDecimal.valueOf(b);
		return da.divide(db, MathContext.DECIMAL128).doubleValue();
	}
	public static double bellNumber(int n) {
		double result = Double.NaN;
		if (n > 1) {
			n -= 1;
			long[][] bellTriangle = new long[n+1][n+1];
			bellTriangle[0][0] = 1;
			bellTriangle[1][0] = 1;
			for (int r = 1; r <= n; r++) {
				for (int k = 0; k < r; k++)
					bellTriangle[r][k+1] = bellTriangle[r-1][k] + bellTriangle[r][k];
				if (r < n)
					bellTriangle[r+1][0] = bellTriangle[r][r];
				if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			}
			result = bellTriangle[n][n];
		} else if (n >= 0)
			result = 1;
		return result;
	}
	public static double bellNumber(double n) {
		if (Double.isNaN(n))
			return Double.NaN;
		return bellNumber( (int)Math.round(n) );
	}
	public static double eulerNumber(int n, int k) {
		if ( n < 0)
			return Double.NaN;
		if (k < 0)
			return 0;
		if (n == 0)
			if (k == 0)
				return 1;
			else
				return 0;
		return (k+1) * eulerNumber(n-1, k) + (n-k) * eulerNumber(n-1, k-1);
	}
	public static double eulerNumber(double n, double k) {
		if (Double.isNaN(n) || Double.isNaN(k))
			return Double.NaN;
		return eulerNumber( (int)Math.round(n), (int)Math.round(k) );
	}
	public static double factorial(int n) {
		double f = Double.NaN;
		if (n >= 0)
			if (n < 2) f = 1;
			else {
				f = 1;
				for (int i = 1; i <= n; i++)
					f = f*i;
			}
		return f;
	}
	public static double factorial(double n) {
		if (Double.isNaN(n))
			return Double.NaN;
		return factorial( (int)Math.round(n) );
	}
	public static double binomCoeff(double n, long k) {
		if (Double.isNaN(n))
			return Double.NaN;
		double result = Double.NaN;
		if ( k >= 0 ){
			double numerator = 1;
			if (k > 0 )
				for (long i = 0; i <= k-1; i++)
					numerator*=(n-i);
			double denominator = 1;
			if ( k > 1 )
				for (long i = 1; i <= k; i++)
					denominator *= i;
			result = numerator / denominator;
		}
		return result;
	}
	public static double binomCoeff(double n, double k) {
		if (Double.isNaN(n) || Double.isNaN(k))
			return Double.NaN;
		return binomCoeff(n, Math.round(k));
	}
	public static double numberOfPermutations(double n, long k) {
		if (Double.isNaN(n))
			return Double.NaN;
		double result = Double.NaN;
		if ( k >= 0 ){
			double numerator = 1;
			if (k > 0 )
				for (long i = 0; i <= k-1; i++)
					numerator*=(n-i);
			result = numerator;
		}
		return result;
	}
	public static double numberOfPermutations(double n, double k) {
		if (Double.isNaN(n) || Double.isNaN(k))
			return Double.NaN;
		return numberOfPermutations(n, Math.round(k));
	}
	public static double bernoulliNumber(int m, int n) {
		double result = Double.NaN;
		if ( (m >= 0) && (n >= 0) ) {
			result = 0;
			for (int k = 0; k <= m; k++)
				for (int v = 0; v <= k; v++) {
					result += Math.pow(-1, v) * binomCoeff(k, v)
						* ( Math.pow(n + v, m) / (k + 1) )
						;
					if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
				}
		}
		return result;
	}
	public static double bernoulliNumber(double m, double n) {
		if (Double.isNaN(m) || Double.isNaN(n))
			return Double.NaN;
		return bernoulliNumber( (int)Math.round(m), (int)Math.round(n) );
	}
	public static double Stirling1Number(int n, int k) {
		if (k > n)
			return 0;
		if (n == 0)
			if (k == 0)
				return 1;
			else
				return 0;
		if (k == 0)
			return 0;
		return (n-1) * Stirling1Number(n-1, k) + Stirling1Number(n-1, k-1);
	}
	public static double Stirling1Number(double n, double k) {
		if (Double.isNaN(n) || Double.isNaN(k))
			return Double.NaN;
		return Stirling1Number( (int)Math.round(n), (int)Math.round(k) );
	}
	public static double Stirling2Number(int n, int k) {
		if (k > n)
			return 0;
		if (n == 0)
			if (k == 0)
				return 1;
			else
				return 0;
		if (k == 0)
			return 0;
		if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		return k * Stirling2Number(n-1, k) + Stirling2Number(n-1, k-1);
	}
	public static double Stirling2Number(double n, double k) {
		if (Double.isNaN(n) || Double.isNaN(k))
			return Double.NaN;
		return Stirling2Number( (int)Math.round(n), (int)Math.round(k) );
	}
	public static double worpitzkyNumber(int n, int k) {
		double result = Double.NaN;
		if ((k >= 0) && (k <= n)){
			result = 0;
			for (int v = 0; v <= k; v++) {
				result += Math.pow(-1, v+k) * Math.pow(v+1, n) * binomCoeff(k, v);
				if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			}
		}
		return result;
	}
	public static double worpitzkyNumber(double n, double k) {
		if (Double.isNaN(n) || Double.isNaN(k))
			return Double.NaN;
		return worpitzkyNumber( (int)Math.round(n), (int)Math.round(k) );
	}
	public static double harmonicNumber(int n) {
		if (n <= 0)
			return 0;
		if (n == 1)
			return 1;
		double h = 1;
		for (double k = 2.0; k <= n; k++)
			h += 1.0 / k;
		return h;
	}
	public static double harmonicNumber(double n) {
		if (Double.isNaN(n))
			return Double.NaN;
		return harmonicNumber( (int)Math.round(n) );
	}
	public static double harmonicNumber(double x, int n) {
		if  ( (Double.isNaN(x)) || (x < 0) )
			return Double.NaN;
		if (n <= 0)
			return 0;
		if (n == 1)
			return x;
		double h = 1;
		for (double k = 2.0; k <= n; k++)
			h += 1 / power(k, x);
		return h;
	}
	public static double harmonicNumber(double x, double n) {
		if ( (Double.isNaN(x)) || (Double.isNaN(n)) )
			return Double.NaN;
		return harmonicNumber( x, (int)Math.round(n) );
	}

	public static double fibonacciNumber(int n) {
		if (n < 0 )
			return Double.NaN;
		if (n == 0)
			return 0;
		if (n == 1)
			return 1;
		if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		return fibonacciNumber(n-1) + fibonacciNumber(n-2);
	}
	public static double fibonacciNumber(double n) {
		if (Double.isNaN(n))
			return Double.NaN;
		return fibonacciNumber( (int)Math.round(n) );
	}
	public static double lucasNumber(int n) {
		if (n < 0 )
			return Double.NaN;
		if (n == 0)
			return 2;
		if (n == 1)
			return 1;
		if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		return lucasNumber(n-1) + lucasNumber(n-2);
	}
	public static double lucasNumber(double n) {
		if (Double.isNaN(n))
			return Double.NaN;
		return lucasNumber( (int)Math.round(n) );
	}
	public static double kroneckerDelta(double i, double j) {
		if (Double.isNaN(i) || Double.isNaN(j))
			return Double.NaN;
		if (i == j)
			return 1;
		else
			return 0;
	}

	public static double continuedFraction(double... sequence) {
		if (sequence == null) return Double.NaN;
		if (sequence.length == 0) return Double.NaN;
		double cf = 0;
		double a;
		if (sequence.length == 1)
			return sequence[0];
		int lastIndex = sequence.length-1;
		for(int i = lastIndex; i >= 0; i--) {
			a = sequence[i];
			if (Double.isNaN(a))
				return Double.NaN;
			if (i == lastIndex) {
				cf = a;
			} else {
				if (cf == 0)
					return Double.NaN;
				cf = a + 1.0 / cf;
			}
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		}
		return cf;
	}
	private static double continuedPolynomial(int n, double[] x) {
		if (x == null) return Double.NaN;
		if (x.length == 0) return Double.NaN;
		if (n == 0)
			return 1;
		if (n == 1)
			return x[0];
		if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		return x[n-1] * continuedPolynomial(n-1, x) + continuedPolynomial(n-2, x);
	}
	public static double continuedPolynomial(double... x) {
		if (x == null) return Double.NaN;
		if (x.length == 0) return Double.NaN;
		for (double d : x)
			if (Double.isNaN(d))
				return Double.NaN;
		return continuedPolynomial(x.length, x);
	}
	public static double eulerPolynomial(int m, double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		double result = Double.NaN;
		if (m >= 0) {
			result = 0;
			for (int n = 0; n <= m; n++) {
				for (int k = 0; k <= n; k++) {
					result += Math.pow(-1, k) * binomCoeff(n, k) * Math.pow(x+k, m);
					if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
				}
				result /= Math.pow(2, n);
			}
		}
		return result;
	}
	public static double eulerPolynomial(double m, double x) {
		if (Double.isNaN(m) || Double.isNaN(x))
			return Double.NaN;
		return eulerPolynomial( (int)Math.round(m), (int)Math.round(x) );
	}
	public static double chi(double x, double a, double b) {
		if (Double.isNaN(x) || Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		double result = Double.NaN;
		if ( (!Double.isNaN(x)) && (!Double.isNaN(a)) && (!Double.isNaN(b)) )
			if ( (x > a) && (x < b) )
				result = 1;
			else
				result = 0;
		return result;
	}
	public static double chi_LR(double x, double a, double b) {
		if (Double.isNaN(x) || Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		double result = Double.NaN;
		if ( (!Double.isNaN(x)) && (!Double.isNaN(a)) && (!Double.isNaN(b)) )
			if ( (x >= a) && (x <= b) )
				result = 1;
			else
				result = 0;
		return result;
	}
	public static double chi_L(double x, double a, double b) {
		if (Double.isNaN(x) || Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		double result = Double.NaN;
		if ( (!Double.isNaN(x)) && (!Double.isNaN(a)) && (!Double.isNaN(b)) )
			if ( (x >= a) && (x < b) )
				result = 1;
			else
				result = 0;
		return result;
	}
	public static double chi_R(double x, double a, double b) {
		if (Double.isNaN(x) || Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		double result = Double.NaN;
		if ( (!Double.isNaN(x)) && (!Double.isNaN(a)) && (!Double.isNaN(b)) )
			if ( (x > a) && (x <= b) )
				result = 1;
			else
				result = 0;
		return result;
	}
	private static double powInt(double a, int n) {
		if (Double.isNaN(a)) return Double.NaN;
		if (a == 0) return Math.pow(a, n);
		if (n == 0) return 1;
		if (n == 1) return a;
		if (mXparser.checkIfCanonicalRounding()) {
			BigDecimal da = BigDecimal.valueOf(a);
			if (n >= 0) return da.pow(n).doubleValue();
			else return BigDecimal.ONE.divide(da, MathContext.DECIMAL128).pow(-n).doubleValue();
		} else {
			return Math.pow(a, n);
		}
	}
	public static double power(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		double babs = Math.abs(b);
		double bint = Math.round(babs);
		if ( MathFunctions.abs(babs - bint) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON ) {
			if (b >= 0) return powInt(a, (int)bint);
			else return powInt(a, -(int)bint);
		} else if (a >= 0)
			return Math.pow(a, b);
		else if (abs(b) >= 1)
			return Math.pow(a, b);
		else if (b == 0)
			return Math.pow(a, b);
		else {
			double ndob = 1.0 / abs(b);
			double nint = Math.round(ndob);
			if ( MathFunctions.abs(ndob-nint) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON ) {
				long n = (long)nint;
				if (n % 2 == 1)
					if (b > 0)
						return -Math.pow( abs(a), 1.0 / ndob);
					else
						return -Math.pow( abs(a), -1.0 / ndob);
				else
					return Double.NaN;
			} else return Double.NaN;
		}
	}
	public static double root(double n, double x) {
		if (Double.isNaN(n)) return Double.NaN;
		if (Double.isInfinite(n)) return Double.NaN;
		if (n < -BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if (abs(n) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) {
			if (abs(x) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return 0;
			else if (abs(x-1) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return 1;
			else return Double.NaN;
		}
		long nint = (long)floor(n);
		if (nint == 1) return x;
		if (nint == 2) return sqrt(x);
		if (nint % 2 == 1) {
			if ( x >= 0) return Math.pow(x, 1.0 / nint);
			else return -Math.pow( abs(x), 1.0 / nint);
		} else {
			if ( x >= 0) return Math.pow(x, 1.0 / nint);
			else return Double.NaN;
		}
	}
	public static double tetration(double a, double n) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(n)) return Double.NaN;
		if (n == Double.POSITIVE_INFINITY) {
			if (abs(a - MathConstants.EXP_MINUS_E) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON)
				return MathConstants.EXP_MINUS_1;
			if (abs(a - MathConstants.EXP_1_OVER_E) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON)
				return MathConstants.E;
			if ((a > MathConstants.EXP_MINUS_E) && (a < MathConstants.EXP_1_OVER_E))
				return SpecialFunctions.lambertW( -MathFunctions.ln(a), 0) / ( -MathFunctions.ln(a) );
			if (a > MathConstants.EXP_1_OVER_E) return Double.POSITIVE_INFINITY;
			if (a < MathConstants.EXP_MINUS_E) return Double.NaN;
		}
		if (n < -BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if (abs(n) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) {
			if (abs(a) > BinaryRelations.DEFAULT_COMPARISON_EPSILON)
				return 1;
			else
				return Double.NaN;
		}
		n = floor(n);
		if (n == 0) {
			if (abs(a) > BinaryRelations.DEFAULT_COMPARISON_EPSILON)
				return 1;
			else
				return Double.NaN;
		}
		if (abs(a) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return 0;
		if (n == 1) return a;
		double r = a;
		for (double i = 2; i <= n; i++)
			r = Math.pow(a, r);
		return r;
	}
	public static double mod(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		return a % b;
	}
	public static double sin(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		if (mXparser.checkIfDegreesMode())
			a = a * Units.DEGREE_ARC;
		SpecialValueTrigonometric sv = SpecialValueTrigonometric.getSpecialValueTrigonometric(a);
		if (sv != null)
			return sv.sin;
		return Math.sin(a);
	}
	public static double cos(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		if (mXparser.checkIfDegreesMode())
			a = a * Units.DEGREE_ARC;
		SpecialValueTrigonometric sv = SpecialValueTrigonometric.getSpecialValueTrigonometric(a);
		if (sv != null)
			return sv.cos;
		return Math.cos(a);
	}
	public static double tan(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		if (mXparser.checkIfDegreesMode())
			a = a * Units.DEGREE_ARC;
		SpecialValueTrigonometric sv = SpecialValueTrigonometric.getSpecialValueTrigonometric(a);
		if (sv != null)
			return sv.tan;
		return Math.tan(a);
	}
	public static double ctan(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		if (mXparser.checkIfDegreesMode())
			a = a * Units.DEGREE_ARC;
		SpecialValueTrigonometric sv = SpecialValueTrigonometric.getSpecialValueTrigonometric(a);
		if (sv != null)
			return sv.ctan;
		double result = Double.NaN;
		double tg = Math.tan(a);
		if (tg != 0)
			result = 1.0 / tg;
		return result;
	}
	public static double sec(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		if (mXparser.checkIfDegreesMode())
			a = a * Units.DEGREE_ARC;
		SpecialValueTrigonometric sv = SpecialValueTrigonometric.getSpecialValueTrigonometric(a);
		if (sv != null)
			return sv.sec;
		double result = Double.NaN;
		double cos = Math.cos(a);
		if (cos != 0)
			result = 1.0 / cos;
		return result;
	}
	public static double cosec(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		if (mXparser.checkIfDegreesMode())
			a = a * Units.DEGREE_ARC;
		SpecialValueTrigonometric sv = SpecialValueTrigonometric.getSpecialValueTrigonometric(a);
		if (sv != null)
			return sv.csc;
		double result = Double.NaN;
		double sin = Math.sin(a);
		if (sin != 0)
			result = 1.0 / sin;
		return result;
	}
	private static double intIfAlmostIntOtherwiseOrig(double val) {
		double valint = Math.round(val);
		if ( Math.abs(val-valint) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON ) return valint;
		return val;
	}
	public static double asin(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		SpecialValue sv = SpecialValueTrigonometric.getSpecialValueAsin(a);
		double r;
		if (sv != null) r = sv.fv;
		else r = Math.asin(a);
		if (mXparser.checkIfDegreesMode()) {
			if (sv != null) return sv.fvdeg;
			return intIfAlmostIntOtherwiseOrig(r / Units.DEGREE_ARC);
		} else return r;
	}
	public static double acos(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		SpecialValue sv = SpecialValueTrigonometric.getSpecialValueAcos(a);
		double r;
		if (sv != null) r = sv.fv;
		else r = Math.acos(a);
		if (mXparser.checkIfDegreesMode()) {
			if (sv != null) return sv.fvdeg;
			return intIfAlmostIntOtherwiseOrig(r / Units.DEGREE_ARC);
		} else return r;
	}
	public static double atan(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		SpecialValue sv = SpecialValueTrigonometric.getSpecialValueAtan(a);
		double r;
		if (sv != null) r = sv.fv;
		else r = Math.atan(a);
		if (mXparser.checkIfDegreesMode()) {
			if (sv != null) return sv.fvdeg;
			return intIfAlmostIntOtherwiseOrig(r / Units.DEGREE_ARC);
		}
		else return r;
	}
	public static double actan(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		SpecialValue sv = SpecialValueTrigonometric.getSpecialValueActan(a);
		double r;
		if (sv != null) r = sv.fv;
		else {
			if (a > 0) r = Math.atan(1/a);
			else if (a < 0) r = Math.atan(1/a) + MathConstants.PI;
			else r = Double.NaN;
		}
		if (mXparser.checkIfDegreesMode()) {
			if (sv != null) return sv.fvdeg;
			return intIfAlmostIntOtherwiseOrig(r / Units.DEGREE_ARC);
		}
		else return r;
	}
	public static double asec(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		SpecialValue sv = SpecialValueTrigonometric.getSpecialValueAsec(a);
		double r;
		if (sv != null) r = sv.fv;
		else r = Math.acos(1/a);
		if (mXparser.checkIfDegreesMode()) {
			if (sv != null) return sv.fvdeg;
			return intIfAlmostIntOtherwiseOrig(r / Units.DEGREE_ARC);
		}
		else return r;
	}
	public static double acosec(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		SpecialValue sv = SpecialValueTrigonometric.getSpecialValueAcsc(a);
		double r;
		if (sv != null) r = sv.fv;
		else r = Math.asin(1/a);
		if (mXparser.checkIfDegreesMode()) {
			if (sv != null) return sv.fvdeg;
			return intIfAlmostIntOtherwiseOrig(r / Units.DEGREE_ARC);
		}
		else return r;
	}
	public static double ln(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.log(a);
	}
	public static double log2(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.log(a)/Math.log(2.0);
	}
	public static double log10(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.log10(a);
	}
	public static double rad(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.toRadians(a);
	}
	public static double exp(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.exp(a);
	}
	 public static double sqrt(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.sqrt(a);
	}
	public static double sinh(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.sinh(a);
	}
	public static double cosh(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.cosh(a);
	}
	public static double tanh(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.tanh(a);
	}
	public static double coth(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		double tanh = Math.tanh(a);
		if (tanh != 0)
			result = 1.0 / tanh;
		return result;
	}
	public static double sech(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		double cosh = Math.cosh(a);
		if (cosh != 0)
			result = 1.0 / cosh;
		return result;
	}
	public static double csch(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		double sinh = Math.sinh(a);
		if (sinh != 0)
			result = 1.0 / sinh;
		return result;
	}
	public static double deg(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.toDegrees(a);
	}
	public static double abs(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.abs(a);
	}
	public static double sgn(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.signum(a);
	}
	public static double floor(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.floor(a);
	}
	public static double ceil(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.ceil(a);
	}
	public static double arsinh(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.log(a + Math.sqrt(a*a+1));
	}
	public static double arcosh(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		return Math.log(a + Math.sqrt(a*a-1));
	}
	public static double artanh(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		if (1-a != 0)
			result = 0.5*Math.log( (1+a)/(1-a) );
		return result;
	}
	public static double arcoth(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		if (a-1 != 0)
			result = 0.5*Math.log( (a+1)/(a-1) );
		return result;
	}
	public static double arsech(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		if (a != 0)
			result = Math.log( (1+Math.sqrt(1-a*a))/a);
		return result;
	}
	public static double arcsch(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result = Double.NaN;
		if (a != 0)
			result = Math.log( 1/a + Math.sqrt(1+a*a)/Math.abs(a) );
		return result;
	}
	public static double sa(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double x = MathConstants.PI * a;
		double result;
		if (x != 0)
			result = Math.sin(x) / (x);
		else
			result =  1.0;
		return result;
	}
	public static double sinc(double a) {
		if (Double.isNaN(a))
			return Double.NaN;
		double result;
		if (a != 0)
			if (mXparser.checkIfDegreesMode())
				result = Math.sin(a * Units.DEGREE_ARC) / (a);
			else
				result = Math.sin(a) / (a);
		else
			result =  1.0;
		return result;
	}
	public static double log(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		double result = Double.NaN;
		double logb = Math.log(b);
		if (logb != 0)
			result = Math.log(a) / logb;
		return result;
	}
 	public static double round(double value, int places) {
		if (Double.isNaN(value)) return Double.NaN;
		if (Double.isInfinite(value)) return value;
		if (places < 0) return Double.NaN;
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
 	}

	public static double integerPart(double x) {
 		if (x > 0) return Math.floor(x);
 		else if (x < 0) return -Math.floor(-x);
 		else return 0;
 	}
	public static int decimalDigitsBefore(double value) {
		if (value == 0) return -1;
		if (value <= 1e-322) return 322;
		else if (value <= 1e-321) return 321;
		else if (value <= 1e-320) return 320;
		else if (value <= 1e-319) return 319;
		else if (value <= 1e-318) return 318;
		else if (value <= 1e-317) return 317;
		else if (value <= 1e-316) return 316;
		else if (value <= 1e-315) return 315;
		else if (value <= 1e-314) return 314;
		else if (value <= 1e-313) return 313;
		else if (value <= 1e-312) return 312;
		else if (value <= 1e-311) return 311;
		else if (value <= 1e-310) return 310;
		else if (value <= 1e-309) return 309;
		else if (value <= 1e-308) return 308;
		else if (value <= 1e-307) return 307;
		else if (value <= 1e-306) return 306;
		else if (value <= 1e-305) return 305;
		else if (value <= 1e-304) return 304;
		else if (value <= 1e-303) return 303;
		else if (value <= 1e-302) return 302;
		else if (value <= 1e-301) return 301;
		else if (value <= 1e-300) return 300;
		else if (value <= 1e-299) return 299;
		else if (value <= 1e-298) return 298;
		else if (value <= 1e-297) return 297;
		else if (value <= 1e-296) return 296;
		else if (value <= 1e-295) return 295;
		else if (value <= 1e-294) return 294;
		else if (value <= 1e-293) return 293;
		else if (value <= 1e-292) return 292;
		else if (value <= 1e-291) return 291;
		else if (value <= 1e-290) return 290;
		else if (value <= 1e-289) return 289;
		else if (value <= 1e-288) return 288;
		else if (value <= 1e-287) return 287;
		else if (value <= 1e-286) return 286;
		else if (value <= 1e-285) return 285;
		else if (value <= 1e-284) return 284;
		else if (value <= 1e-283) return 283;
		else if (value <= 1e-282) return 282;
		else if (value <= 1e-281) return 281;
		else if (value <= 1e-280) return 280;
		else if (value <= 1e-279) return 279;
		else if (value <= 1e-278) return 278;
		else if (value <= 1e-277) return 277;
		else if (value <= 1e-276) return 276;
		else if (value <= 1e-275) return 275;
		else if (value <= 1e-274) return 274;
		else if (value <= 1e-273) return 273;
		else if (value <= 1e-272) return 272;
		else if (value <= 1e-271) return 271;
		else if (value <= 1e-270) return 270;
		else if (value <= 1e-269) return 269;
		else if (value <= 1e-268) return 268;
		else if (value <= 1e-267) return 267;
		else if (value <= 1e-266) return 266;
		else if (value <= 1e-265) return 265;
		else if (value <= 1e-264) return 264;
		else if (value <= 1e-263) return 263;
		else if (value <= 1e-262) return 262;
		else if (value <= 1e-261) return 261;
		else if (value <= 1e-260) return 260;
		else if (value <= 1e-259) return 259;
		else if (value <= 1e-258) return 258;
		else if (value <= 1e-257) return 257;
		else if (value <= 1e-256) return 256;
		else if (value <= 1e-255) return 255;
		else if (value <= 1e-254) return 254;
		else if (value <= 1e-253) return 253;
		else if (value <= 1e-252) return 252;
		else if (value <= 1e-251) return 251;
		else if (value <= 1e-250) return 250;
		else if (value <= 1e-249) return 249;
		else if (value <= 1e-248) return 248;
		else if (value <= 1e-247) return 247;
		else if (value <= 1e-246) return 246;
		else if (value <= 1e-245) return 245;
		else if (value <= 1e-244) return 244;
		else if (value <= 1e-243) return 243;
		else if (value <= 1e-242) return 242;
		else if (value <= 1e-241) return 241;
		else if (value <= 1e-240) return 240;
		else if (value <= 1e-239) return 239;
		else if (value <= 1e-238) return 238;
		else if (value <= 1e-237) return 237;
		else if (value <= 1e-236) return 236;
		else if (value <= 1e-235) return 235;
		else if (value <= 1e-234) return 234;
		else if (value <= 1e-233) return 233;
		else if (value <= 1e-232) return 232;
		else if (value <= 1e-231) return 231;
		else if (value <= 1e-230) return 230;
		else if (value <= 1e-229) return 229;
		else if (value <= 1e-228) return 228;
		else if (value <= 1e-227) return 227;
		else if (value <= 1e-226) return 226;
		else if (value <= 1e-225) return 225;
		else if (value <= 1e-224) return 224;
		else if (value <= 1e-223) return 223;
		else if (value <= 1e-222) return 222;
		else if (value <= 1e-221) return 221;
		else if (value <= 1e-220) return 220;
		else if (value <= 1e-219) return 219;
		else if (value <= 1e-218) return 218;
		else if (value <= 1e-217) return 217;
		else if (value <= 1e-216) return 216;
		else if (value <= 1e-215) return 215;
		else if (value <= 1e-214) return 214;
		else if (value <= 1e-213) return 213;
		else if (value <= 1e-212) return 212;
		else if (value <= 1e-211) return 211;
		else if (value <= 1e-210) return 210;
		else if (value <= 1e-209) return 209;
		else if (value <= 1e-208) return 208;
		else if (value <= 1e-207) return 207;
		else if (value <= 1e-206) return 206;
		else if (value <= 1e-205) return 205;
		else if (value <= 1e-204) return 204;
		else if (value <= 1e-203) return 203;
		else if (value <= 1e-202) return 202;
		else if (value <= 1e-201) return 201;
		else if (value <= 1e-200) return 200;
		else if (value <= 1e-199) return 199;
		else if (value <= 1e-198) return 198;
		else if (value <= 1e-197) return 197;
		else if (value <= 1e-196) return 196;
		else if (value <= 1e-195) return 195;
		else if (value <= 1e-194) return 194;
		else if (value <= 1e-193) return 193;
		else if (value <= 1e-192) return 192;
		else if (value <= 1e-191) return 191;
		else if (value <= 1e-190) return 190;
		else if (value <= 1e-189) return 189;
		else if (value <= 1e-188) return 188;
		else if (value <= 1e-187) return 187;
		else if (value <= 1e-186) return 186;
		else if (value <= 1e-185) return 185;
		else if (value <= 1e-184) return 184;
		else if (value <= 1e-183) return 183;
		else if (value <= 1e-182) return 182;
		else if (value <= 1e-181) return 181;
		else if (value <= 1e-180) return 180;
		else if (value <= 1e-179) return 179;
		else if (value <= 1e-178) return 178;
		else if (value <= 1e-177) return 177;
		else if (value <= 1e-176) return 176;
		else if (value <= 1e-175) return 175;
		else if (value <= 1e-174) return 174;
		else if (value <= 1e-173) return 173;
		else if (value <= 1e-172) return 172;
		else if (value <= 1e-171) return 171;
		else if (value <= 1e-170) return 170;
		else if (value <= 1e-169) return 169;
		else if (value <= 1e-168) return 168;
		else if (value <= 1e-167) return 167;
		else if (value <= 1e-166) return 166;
		else if (value <= 1e-165) return 165;
		else if (value <= 1e-164) return 164;
		else if (value <= 1e-163) return 163;
		else if (value <= 1e-162) return 162;
		else if (value <= 1e-161) return 161;
		else if (value <= 1e-160) return 160;
		else if (value <= 1e-159) return 159;
		else if (value <= 1e-158) return 158;
		else if (value <= 1e-157) return 157;
		else if (value <= 1e-156) return 156;
		else if (value <= 1e-155) return 155;
		else if (value <= 1e-154) return 154;
		else if (value <= 1e-153) return 153;
		else if (value <= 1e-152) return 152;
		else if (value <= 1e-151) return 151;
		else if (value <= 1e-150) return 150;
		else if (value <= 1e-149) return 149;
		else if (value <= 1e-148) return 148;
		else if (value <= 1e-147) return 147;
		else if (value <= 1e-146) return 146;
		else if (value <= 1e-145) return 145;
		else if (value <= 1e-144) return 144;
		else if (value <= 1e-143) return 143;
		else if (value <= 1e-142) return 142;
		else if (value <= 1e-141) return 141;
		else if (value <= 1e-140) return 140;
		else if (value <= 1e-139) return 139;
		else if (value <= 1e-138) return 138;
		else if (value <= 1e-137) return 137;
		else if (value <= 1e-136) return 136;
		else if (value <= 1e-135) return 135;
		else if (value <= 1e-134) return 134;
		else if (value <= 1e-133) return 133;
		else if (value <= 1e-132) return 132;
		else if (value <= 1e-131) return 131;
		else if (value <= 1e-130) return 130;
		else if (value <= 1e-129) return 129;
		else if (value <= 1e-128) return 128;
		else if (value <= 1e-127) return 127;
		else if (value <= 1e-126) return 126;
		else if (value <= 1e-125) return 125;
		else if (value <= 1e-124) return 124;
		else if (value <= 1e-123) return 123;
		else if (value <= 1e-122) return 122;
		else if (value <= 1e-121) return 121;
		else if (value <= 1e-120) return 120;
		else if (value <= 1e-119) return 119;
		else if (value <= 1e-118) return 118;
		else if (value <= 1e-117) return 117;
		else if (value <= 1e-116) return 116;
		else if (value <= 1e-115) return 115;
		else if (value <= 1e-114) return 114;
		else if (value <= 1e-113) return 113;
		else if (value <= 1e-112) return 112;
		else if (value <= 1e-111) return 111;
		else if (value <= 1e-110) return 110;
		else if (value <= 1e-109) return 109;
		else if (value <= 1e-108) return 108;
		else if (value <= 1e-107) return 107;
		else if (value <= 1e-106) return 106;
		else if (value <= 1e-105) return 105;
		else if (value <= 1e-104) return 104;
		else if (value <= 1e-103) return 103;
		else if (value <= 1e-102) return 102;
		else if (value <= 1e-101) return 101;
		else if (value <= 1e-100) return 100;
		else if (value <= 1e-99) return 99;
		else if (value <= 1e-98) return 98;
		else if (value <= 1e-97) return 97;
		else if (value <= 1e-96) return 96;
		else if (value <= 1e-95) return 95;
		else if (value <= 1e-94) return 94;
		else if (value <= 1e-93) return 93;
		else if (value <= 1e-92) return 92;
		else if (value <= 1e-91) return 91;
		else if (value <= 1e-90) return 90;
		else if (value <= 1e-89) return 89;
		else if (value <= 1e-88) return 88;
		else if (value <= 1e-87) return 87;
		else if (value <= 1e-86) return 86;
		else if (value <= 1e-85) return 85;
		else if (value <= 1e-84) return 84;
		else if (value <= 1e-83) return 83;
		else if (value <= 1e-82) return 82;
		else if (value <= 1e-81) return 81;
		else if (value <= 1e-80) return 80;
		else if (value <= 1e-79) return 79;
		else if (value <= 1e-78) return 78;
		else if (value <= 1e-77) return 77;
		else if (value <= 1e-76) return 76;
		else if (value <= 1e-75) return 75;
		else if (value <= 1e-74) return 74;
		else if (value <= 1e-73) return 73;
		else if (value <= 1e-72) return 72;
		else if (value <= 1e-71) return 71;
		else if (value <= 1e-70) return 70;
		else if (value <= 1e-69) return 69;
		else if (value <= 1e-68) return 68;
		else if (value <= 1e-67) return 67;
		else if (value <= 1e-66) return 66;
		else if (value <= 1e-65) return 65;
		else if (value <= 1e-64) return 64;
		else if (value <= 1e-63) return 63;
		else if (value <= 1e-62) return 62;
		else if (value <= 1e-61) return 61;
		else if (value <= 1e-60) return 60;
		else if (value <= 1e-59) return 59;
		else if (value <= 1e-58) return 58;
		else if (value <= 1e-57) return 57;
		else if (value <= 1e-56) return 56;
		else if (value <= 1e-55) return 55;
		else if (value <= 1e-54) return 54;
		else if (value <= 1e-53) return 53;
		else if (value <= 1e-52) return 52;
		else if (value <= 1e-51) return 51;
		else if (value <= 1e-50) return 50;
		else if (value <= 1e-49) return 49;
		else if (value <= 1e-48) return 48;
		else if (value <= 1e-47) return 47;
		else if (value <= 1e-46) return 46;
		else if (value <= 1e-45) return 45;
		else if (value <= 1e-44) return 44;
		else if (value <= 1e-43) return 43;
		else if (value <= 1e-42) return 42;
		else if (value <= 1e-41) return 41;
		else if (value <= 1e-40) return 40;
		else if (value <= 1e-39) return 39;
		else if (value <= 1e-38) return 38;
		else if (value <= 1e-37) return 37;
		else if (value <= 1e-36) return 36;
		else if (value <= 1e-35) return 35;
		else if (value <= 1e-34) return 34;
		else if (value <= 1e-33) return 33;
		else if (value <= 1e-32) return 32;
		else if (value <= 1e-31) return 31;
		else if (value <= 1e-30) return 30;
		else if (value <= 1e-29) return 29;
		else if (value <= 1e-28) return 28;
		else if (value <= 1e-27) return 27;
		else if (value <= 1e-26) return 26;
		else if (value <= 1e-25) return 25;
		else if (value <= 1e-24) return 24;
		else if (value <= 1e-23) return 23;
		else if (value <= 1e-22) return 22;
		else if (value <= 1e-21) return 21;
		else if (value <= 1e-20) return 20;
		else if (value <= 1e-19) return 19;
		else if (value <= 1e-18) return 18;
		else if (value <= 1e-17) return 17;
		else if (value <= 1e-16) return 16;
		else if (value <= 1e-15) return 15;
		else if (value <= 1e-14) return 14;
		else if (value <= 1e-13) return 13;
		else if (value <= 1e-12) return 12;
		else if (value <= 1e-11) return 11;
		else if (value <= 1e-10) return 10;
		else if (value <= 1e-9) return 9;
		else if (value <= 1e-8) return 8;
		else if (value <= 1e-7) return 7;
		else if (value <= 1e-6) return 6;
		else if (value <= 1e-5) return 5;
		else if (value <= 1e-4) return 4;
		else if (value <= 1e-3) return 3;
		else if (value <= 1e-2) return 2;
		else if (value <= 1e-1) return 1;
		else if (value <= 1e-0) return 0;
		else return -1;
	}
	public static double ulp(double value) {
		return Math.ulp(value);
	}
	public static int ulpDecimalDigitsBefore(double value) {
		if (Double.isNaN(value)) return -2;
		double u = ulp(value);
		return decimalDigitsBefore(u);
	}
	public static double coalesce(double[] values) {
		if (values == null) return Double.NaN;
		if (values.length == 0) return Double.NaN;
		for (double v : values)
			if (!Double.isNaN(v)) return v;
		return Double.NaN;
	}
	public static boolean isInteger(double x) {
		if (Double.isNaN(x)) return false;
		if (x == Double.POSITIVE_INFINITY) return false;
		if (x == Double.NEGATIVE_INFINITY) return false;
		if (x < 0) x = -x;
		double round = Math.round(x);
		return Math.abs(x - round) < BinaryRelations.DEFAULT_COMPARISON_EPSILON;
	}
	public static boolean almostEqual(double a, double b) {
		if (Double.isNaN(a)) return false;
		if (Double.isNaN(b)) return false;
		if (a == b) return true;
		return Math.abs(a - b) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON;
	}
}
