package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;

public final class Statistics {
	public static double avg(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) return numbers[0];
		double sum = 0;
		for (double xi : numbers) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			if ( Double.isNaN(xi) )
				return Double.NaN;
			sum+=xi;
		}
		return sum / numbers.length;
	}
	public static double var(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) {
			if (Double.isNaN(numbers[0])) return Double.NaN;
			return 0;
		}
		double m = avg(numbers);
		double sum = 0;
		for (double xi : numbers) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			if ( Double.isNaN(xi) )
				return Double.NaN;
			sum+=(xi-m)*(xi-m);
		}
		return sum / (numbers.length - 1);
	}
	public static double std(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) {
			if (Double.isNaN(numbers[0])) return Double.NaN;
			return 0;
		}
		return MathFunctions.sqrt( var(numbers) );
	}
	public static double median(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) return numbers[0];
		if (numbers.length == 2) return (numbers[0] + numbers[1]) / 2.0;
		for (double v : numbers) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			if (Double.isNaN(v)) return Double.NaN;
		}
		NumberTheory.sortAsc(numbers);
		if ((numbers.length % 2) == 1) {
			int i = (numbers.length-1) / 2;
			return numbers[i];
		} else {
			int i = ( numbers.length / 2 ) - 1;
			return (numbers[i] + numbers[i+1]) / 2.0;
		}
	}
	public static double mode(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) return numbers[0];
		for (double v : numbers) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			if (Double.isNaN(v)) return Double.NaN;
		}
		double[][] dist = NumberTheory.getDistValues(numbers, true);
		return dist[0][0];
	}
}
