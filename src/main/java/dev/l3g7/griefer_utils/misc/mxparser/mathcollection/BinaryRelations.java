package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

public final class BinaryRelations {
	public static double DEFAULT_COMPARISON_EPSILON = 0.00000000000001;
	static double epsilon = DEFAULT_COMPARISON_EPSILON;
	static boolean epsilonComparison = true;
	public static double getEpsilon() {
		return epsilon;
	}
	public static double eq(double a, double b) {
		if ( ( Double.isNaN(a) ) || ( Double.isNaN(b) ) ) return Double.NaN;
		double eps = NumberTheory.max(epsilon, MathFunctions.ulp(b) );
		if ( Double.isInfinite(a) || Double.isInfinite(b) ) eps = 0;
		double result = BooleanAlgebra.FALSE;
		if (epsilonComparison) {
			if ( MathFunctions.abs(a-b) <= eps ) result = BooleanAlgebra.TRUE;
		} else if ( a == b ) result = BooleanAlgebra.TRUE;
		return result;
	}
	public static double neq(double a, double b) {
		if ( ( Double.isNaN(a) ) || ( Double.isNaN(b) ) ) return Double.NaN;
		double eps = NumberTheory.max(epsilon, MathFunctions.ulp(b) );
		if ( Double.isInfinite(a) || Double.isInfinite(b) ) eps = 0;
		double result = BooleanAlgebra.FALSE;
		if (epsilonComparison) {
			if ( MathFunctions.abs(a-b) > eps ) result = BooleanAlgebra.TRUE;
		} else if ( a != b ) result = BooleanAlgebra.TRUE;
		return result;
	}
	public static double lt(double a, double b) {
		if ( ( Double.isNaN(a) ) || ( Double.isNaN(b) ) ) return Double.NaN;
		double eps = NumberTheory.max(epsilon, MathFunctions.ulp(b) );
		if ( Double.isInfinite(a) || Double.isInfinite(b) ) eps = 0;
		double result = BooleanAlgebra.FALSE;
		if (epsilonComparison) {
			if ( a < b - eps ) result = BooleanAlgebra.TRUE;
		} else if ( a < b ) result = BooleanAlgebra.TRUE;
		return result;
	}
	public static double gt(double a, double b) {
		if ( ( Double.isNaN(a) ) || ( Double.isNaN(b) ) ) return Double.NaN;
		double eps = NumberTheory.max(epsilon, MathFunctions.ulp(b) );
		if ( Double.isInfinite(a) || Double.isInfinite(b) ) eps = 0;
		double result = BooleanAlgebra.FALSE;
		if (epsilonComparison) {
			if ( a > b + eps ) result = BooleanAlgebra.TRUE;
		} else if ( a > b ) result = BooleanAlgebra.TRUE;
		return result;
	}
	public static double leq(double a, double b) {
		if ( ( Double.isNaN(a) ) || ( Double.isNaN(b) ) ) return Double.NaN;
		double eps = NumberTheory.max(epsilon, MathFunctions.ulp(b) );
		if ( Double.isInfinite(a) || Double.isInfinite(b) ) eps = 0;
		double result = BooleanAlgebra.FALSE;
		if (epsilonComparison) {
			if ( a <= b + eps ) result = BooleanAlgebra.TRUE;
		} else if ( a <= b ) result = BooleanAlgebra.TRUE;
		return result;
	}
	public static double geq(double a, double b) {
		if ( ( Double.isNaN(a) ) || ( Double.isNaN(b) ) ) return Double.NaN;
		double eps = NumberTheory.max(epsilon, MathFunctions.ulp(b) );
		if ( Double.isInfinite(a) || Double.isInfinite(b) ) eps = 0;
		double result = BooleanAlgebra.FALSE;
		if (epsilonComparison) {
			if ( a >= b - eps ) result = BooleanAlgebra.TRUE;
		} else if ( a >= b ) result = BooleanAlgebra.TRUE;
		return result;
	}
}