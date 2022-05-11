package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

public final class Evaluate {
	public static double polynomial(double x, double[] coefficients) {
		if (Double.isNaN(x)) return Double.NaN;
		if (coefficients == null) return Double.NaN;
		if (coefficients.length == 0) return Double.NaN;
		if (coefficients.length == 1) return coefficients[0];
		double sum = coefficients[coefficients.length - 1];
		if (Double.isNaN(sum)) return Double.NaN;
		for (int i = coefficients.length - 2; i >= 0; i--) {
			if (Double.isNaN(coefficients[i])) return Double.NaN;
			sum *= x;
			sum += coefficients[i];
		}
        return sum;
	}
	public static double p1evl(double x, double[] coef, int n) {
		double ans;
		ans = x + coef[0];
		for(int i=1; i<n; i++) { ans = ans*x+coef[i]; }
		return ans;
	}
	public static double polevl(double x, double[] coef, int n) {
		double ans;
		ans = coef[0];
		for(int i=1; i<=n; i++) ans = ans*x+coef[i];
		return ans;
	}
}
