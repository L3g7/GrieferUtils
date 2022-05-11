package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;

public final class NumberTheory {

	public static double min(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		return Math.min(a, b);
	}
	public static double min(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		double min = Double.POSITIVE_INFINITY;
		for (double number : numbers) {
			if (Double.isNaN(number))
				return Double.NaN;
			if (number < min)
				min = number;
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		}
		return min;
	}
	public static double argmin(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		double min = Double.POSITIVE_INFINITY;
		double minIndex = -1;
		for (int i = 0; i < numbers.length; i++) {
			double number = numbers[i];
			if (Double.isNaN(number))
				return Double.NaN;
			if (BinaryRelations.lt(number, min) == BooleanAlgebra.TRUE) {
				min = number;
				minIndex = i;
			}
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		}
		return minIndex + 1;
	}
	public static double max(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b))
			return Double.NaN;
		return Math.max(a, b);
	}
	public static double max(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		double max = Double.NEGATIVE_INFINITY;
		for (double number : numbers) {
			if (Double.isNaN(number))
				return Double.NaN;
			if (number > max)
				max = number;
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		}
		return max;
	}
	public static double argmax(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		double max = Double.NEGATIVE_INFINITY;
		double maxIndex = -1;
		for (int i = 0; i < numbers.length; i++) {
			double number = numbers[i];
			if (Double.isNaN(number))
				return Double.NaN;
			if (BinaryRelations.gt(number, max) == BooleanAlgebra.TRUE) {
				max = number;
				maxIndex = i;
			}
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
		}
		return maxIndex + 1;
	}
	private static void sortAsc(double[] array, int[] initOrder, int leftIndex, int rightIndex) {
		int i = leftIndex;
		int j = rightIndex;
		double x = array[(leftIndex+rightIndex)/2];
		double w;
		int v;
		do {

			while ( BinaryRelations.lt(array[i], x) == BooleanAlgebra.TRUE ) i++;
			while ( BinaryRelations.gt(array[j], x) == BooleanAlgebra.TRUE ) j--;
			if (i <= j) {
				w = array[i];
				array[i] = array[j];
				array[j] = w;
				v = initOrder[i];
				initOrder[i] = initOrder[j];
				initOrder[j] = v;
				i++;
				j--;
			}
			if (mXparser.isCurrentCalculationCancelled()) return;
		} while (i <= j);
		if (leftIndex < j) sortAsc(array, initOrder, leftIndex, j);
		if (i < rightIndex) sortAsc(array, initOrder, i, rightIndex);
	}
	public static int[] sortAsc(double[] array) {
		if (array == null) return null;
		int[] initOrder = new int[array.length];
		for (int i = 0; i < array.length; i++)
			initOrder[i] = i;
		if (array.length < 2) return initOrder;
		sortAsc(array, initOrder, 0, array.length-1);
		return initOrder;
	}
	public static double[][] getDistValues(double[] array, boolean returnOrderByDescFreqAndAscOrigPos) {
		if (array == null) return null;
		final int value = 0;
		final int count = 1;
		final int initPosFirst = 2;
		double[][] distVal = new double[array.length][3];
		if (array.length == 0) return distVal;
		if (array.length == 1) {
			distVal[0][value] = array[0];
			distVal[0][count] = 1;
			distVal[0][initPosFirst] = 0;
			return distVal;
		}
		int[] initPos = sortAsc(array);
		double unqValue = array[0];
		int unqValCnt = 1;
		int unqValMinPos = initPos[0];
		int unqCnt = 0;
		for (int i = 1; i < array.length; i++) {
			if (mXparser.isCurrentCalculationCancelled()) break;
			if ( BinaryRelations.eq(unqValue, array[i]) == BooleanAlgebra.TRUE ) {
				unqValCnt++;
				if (initPos[i] < unqValMinPos)
					unqValMinPos = initPos[i];
			}
			if ( ( BinaryRelations.eq(unqValue, array[i]) == BooleanAlgebra.FALSE ) && (i < array.length-1) ) {
				distVal[unqCnt][value] = unqValue;
				distVal[unqCnt][count] = unqValCnt;
				distVal[unqCnt][initPosFirst] = unqValMinPos;
				unqCnt++;
				unqValue = array[i];
				unqValCnt = 1;
				unqValMinPos = initPos[i];
			} else if ( ( BinaryRelations.eq(unqValue, array[i]) == BooleanAlgebra.FALSE ) && (i == array.length-1) ) {
				distVal[unqCnt][value] = unqValue;
				distVal[unqCnt][count] = unqValCnt;
				distVal[unqCnt][initPosFirst] = unqValMinPos;
				unqCnt++;
				distVal[unqCnt][value] = array[i];
				distVal[unqCnt][count] = 1;
				distVal[unqCnt][initPosFirst] = initPos[i];
				unqCnt++;
			} else if (i == array.length-1) {
				distVal[unqCnt][value] = unqValue;
				distVal[unqCnt][count] = unqValCnt;
				distVal[unqCnt][initPosFirst] = unqValMinPos;
				unqCnt++;
			}
		}
		double[][] distValFinal = new double[unqCnt][3];
		double maxBase = 0;
		for (int i = 0; i < unqCnt; i++) {
			if (mXparser.isCurrentCalculationCancelled()) break;
			distValFinal[i][value] = distVal[i][value];
			distValFinal[i][count] = distVal[i][count];
			distValFinal[i][initPosFirst] = distVal[i][initPosFirst];
			if ( distVal[i][count] > maxBase) maxBase = distVal[i][count];
			if ( distVal[i][initPosFirst] > maxBase) maxBase = distVal[i][initPosFirst];
		}
		if (!returnOrderByDescFreqAndAscOrigPos) return distValFinal;
		maxBase++;
		double[] key = new double[unqCnt];
		for (int i = 0; i < unqCnt; i++) {
			if (mXparser.isCurrentCalculationCancelled()) break;
			key[i] = (maxBase - distVal[i][count] - 1) * maxBase + distVal[i][initPosFirst];
		}
		int[] keyInitOrder = sortAsc(key);
		for (int i = 0; i < unqCnt; i++) {
			if (mXparser.isCurrentCalculationCancelled()) break;
			distValFinal[i][value] = distVal[ keyInitOrder[i] ][value];
			distValFinal[i][count] = distVal[ keyInitOrder[i] ][count];
			distValFinal[i][initPosFirst] = distVal[ keyInitOrder[i] ][initPosFirst];
		}
		return distValFinal;
	}
	public static double numberOfDistValues(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return 0;
		for (double v : numbers)
			if (Double.isNaN(v)) return Double.NaN;
		if (numbers.length == 1) return 1;
		return getDistValues(numbers, false).length;
	}

	public static double gcd(double a, double b) {
		if ( Double.isNaN(a) )
			return Double.NaN;
		if (a < 0) a = -a;
		if (b < 0) b = -b;
		a = MathFunctions.floor( MathFunctions.abs(a) );
		b = MathFunctions.floor( MathFunctions.abs(b) );
		if ( (a == 0) && (b != 0) ) return b;
		if ( (a != 0) && (b == 0) ) return a;
		if (a == 0) return Double.NaN;
		if (a == b) return a;
		double quotient;
		while (b != 0.0) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			if (a > b) {
				quotient = Math.floor(a / b) - 1;
				if (quotient > 0)
					a = Math.floor(a - b*quotient);
				else
					a = Math.floor(a - b);
			} else {
				quotient = Math.floor(b / a) - 1;
				if (quotient > 0)
					b = Math.floor(b - a*quotient);
				else
					b = Math.floor(b - a);
			}
		}
		return a;
	}
	public static double gcd(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1)
			return MathFunctions.floor( MathFunctions.abs( numbers[0] ) );
		if (numbers.length == 2)
			return gcd( numbers[0], numbers[1] );
		for (int i = 1; i < numbers.length; i++) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			numbers[i] = gcd( numbers[i-1], numbers[i] );
		}
		return numbers[numbers.length-1];
	}

	public static double lcm(double a, double b) {
		if ( Double.isNaN(a) )
			return Double.NaN;
		a = MathFunctions.floor( MathFunctions.abs(a) );
		b = MathFunctions.floor( MathFunctions.abs(b) );
		return (a*b) / gcd(a, b);
	}
	public static double lcm(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1)
			MathFunctions.floor( MathFunctions.abs( numbers[0] ) );
		if (numbers.length == 2)
			return lcm( numbers[0], numbers[1] );
		for (int i = 1; i < numbers.length; i++) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			numbers[i] = lcm( numbers[i-1], numbers[i] );
		}
		return numbers[numbers.length-1];
	}
	public static double sum(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) return numbers[0];
		if (mXparser.checkIfCanonicalRounding()) {
			BigDecimal dsum = BigDecimal.ZERO;
			for (double xi : numbers) {
				if ( Double.isNaN(xi) ) return Double.NaN;
				if ( Double.isInfinite(xi)) return Double.NaN;
				dsum = dsum.add(BigDecimal.valueOf(xi));
				if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			}
			return dsum.doubleValue();
		} else {
			double sum = 0;
			for (double xi : numbers) {
				if ( Double.isNaN(xi) ) return Double.NaN;
				if ( Double.isInfinite(xi)) return Double.NaN;
				sum += xi;
				if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			}
			return sum;
		}
	}
	public static double prod(double... numbers) {
		if (numbers == null) return Double.NaN;
		if (numbers.length == 0) return Double.NaN;
		if (numbers.length == 1) return numbers[0];
		if (mXparser.checkIfCanonicalRounding()) {
			BigDecimal dprod = BigDecimal.ONE;
			for (double xi : numbers) {
				if ( Double.isNaN(xi) ) return Double.NaN;
				if ( Double.isInfinite(xi)) return Double.NaN;
				dprod = dprod.multiply(BigDecimal.valueOf(xi));
				if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			}
			return dprod.doubleValue();
		} else {
			double prod = 1;
			for (double xi : numbers) {
				if ( Double.isNaN(xi) ) return Double.NaN;
				if ( Double.isInfinite(xi)) return Double.NaN;
				prod *= xi;
				if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			}
			return prod;
		}
	}

	public static boolean primeTest(long n) {
		if (n == 2) return true;
		if (n % 2 == 0) return false;
		if (n <= 1) return false;
		long top = (long)Math.sqrt(n);
		long primesCacheOddEnd = 3;
		if (mXparser.primesCache != null)
			if ( mXparser.primesCache.cacheStatus == PrimesCache.CACHING_FINISHED ) {
					if ( n <= mXparser.primesCache.maxNumInCache )
						return mXparser.primesCache.isPrime[(int)n];
					else {
						long topCache = Math.min(top, mXparser.primesCache.maxNumInCache);
						long i;
						for (i = 3; i <= topCache; i+=2) {
							if (mXparser.primesCache.isPrime[(int) i])
								if (n % i == 0) return false;
							if (mXparser.isCurrentCalculationCancelled()) return false;
						}
						primesCacheOddEnd = i;
					}
			}
		for (long i = primesCacheOddEnd; i <= top; i+=2) {
			if (n % i == 0) return false;
			if (mXparser.isCurrentCalculationCancelled()) return false;
		}
		return true;
	}
	public static double primeTest(double n) {
		if ( Double.isNaN(n) ) return Double.NaN;
		boolean isPrime = primeTest((long)n);
		if (isPrime)
			return 1;
		else
			return 0;
	}
	public static long primeCount(long n) {
		if (n <= 1) return 0;
		if (n == 2) return 1;
		long numberOfPrimes = 1;
		for (long i = 3; i <= n; i++) {
			if (mXparser.isCurrentCalculationCancelled()) return 0;
			if(primeTest(i))
				numberOfPrimes++;
		}
		return numberOfPrimes;
	}
	public static double primeCount(double n) {
		return primeCount((long)n);
	}

	public static int digitIndex(char digitChar) {
		switch (digitChar) {
			case '0': return 0;
			case '1': return 1;
			case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			case 'A': return 10;
			case 'B': return 11;
			case 'C': return 12;
			case 'D': return 13;
			case 'E': return 14;
			case 'F': return 15;
			case 'G': return 16;
			case 'H': return 17;
			case 'I': return 18;
			case 'J': return 19;
			case 'K': return 20;
			case 'L': return 21;
			case 'M': return 22;
			case 'N': return 23;
			case 'O': return 24;
			case 'P': return 25;
			case 'Q': return 26;
			case 'R': return 27;
			case 'S': return 28;
			case 'T': return 29;
			case 'U': return 30;
			case 'V': return 31;
			case 'W': return 32;
			case 'X': return 33;
			case 'Y': return 34;
			case 'Z': return 35;
			case 'a': return 10;
			case 'b': return 11;
			case 'c': return 12;
			case 'd': return 13;
			case 'e': return 14;
			case 'f': return 15;
			case 'g': return 16;
			case 'h': return 17;
			case 'i': return 18;
			case 'j': return 19;
			case 'k': return 20;
			case 'l': return 21;
			case 'm': return 22;
			case 'n': return 23;
			case 'o': return 24;
			case 'p': return 25;
			case 'q': return 26;
			case 'r': return 27;
			case 's': return 28;
			case 't': return 29;
			case 'u': return 30;
			case 'v': return 31;
			case 'w': return 32;
			case 'x': return 33;
			case 'y': return 34;
			case 'z': return 35;
		}
		return -1;
	}

	public static double convOthBase2Decimal(String numberLiteral, int numeralSystemBase) {
		if (numberLiteral == null) return Double.NaN;
		numberLiteral = numberLiteral.trim();
		if (numberLiteral.length() == 0) {
			if (numeralSystemBase == 1) return 0;
			else return Double.NaN;
		}
		if (numeralSystemBase < 1) return Double.NaN;
		if (numeralSystemBase > 36) return Double.NaN;
		char signChar = numberLiteral.charAt(0);
		double sign = 1.0;
		if (signChar == '-') {
			sign = -1.0;
			numberLiteral = numberLiteral.substring(1);
		} else if (signChar == '+') {
			numberLiteral = numberLiteral.substring(1);
		}
		int length = numberLiteral.length();
		double decValue = 0;
		int digit;
		for (int i = 0; i < length; i++ ) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			digit = digitIndex( numberLiteral.charAt(i) );
			if (numeralSystemBase > 1) {
				if ( (digit >= 0) && (digit < numeralSystemBase) ) decValue = numeralSystemBase * decValue + digit;
				else return Double.NaN;
			} else {
				if (digit == 1) decValue = numeralSystemBase * decValue + digit;
				else return Double.NaN;
			}
		}
		return sign * decValue;
	}
	public static double convOthBase2Decimal(int numeralSystemBase, int... digits) {
		if (numeralSystemBase < 1) return Double.NaN;
		if (digits == null) return Double.NaN;
		int length = digits.length;
		if (length == 0) {
			if (numeralSystemBase == 1) return 0;
			else return Double.NaN;
		}
		double decValue = 0;
		int digit;
		for (int j : digits) {
			digit = j;
			if (numeralSystemBase > 1) {
				if ((digit >= 0) && (digit < numeralSystemBase)) decValue = numeralSystemBase * decValue + digit;
				else return Double.NaN;
			} else {
				if (digit == 1) decValue = numeralSystemBase * decValue + digit;
				else return Double.NaN;
			}
		}
		return decValue;
	}
	public static double convOthBase2Decimal(double numeralSystemBase, double... digits) {
		if (numeralSystemBase < 0) return Double.NaN;
		if (Double.isNaN(numeralSystemBase)) return Double.NaN;
		int numeralSystemBaseInt = (int)MathFunctions.floor(numeralSystemBase);
		if (digits == null) return Double.NaN;
		int length = digits.length;
		if (length == 0) {
			if (numeralSystemBaseInt == 1) return 0;
			else return Double.NaN;
		}
		int[] digitsInt = new int[length];
		double digit;
		for (int i = 0; i < length; i++ ) {
			digit = digits[i];
			if (Double.isNaN(digit)) return Double.NaN;
			digitsInt[i] = (int)digit;
		}
		return convOthBase2Decimal(numeralSystemBaseInt, digitsInt);
	}

	public static double convOthBase2Decimal(double[] baseAndDigits) {
		if (baseAndDigits == null) return Double.NaN;
		if (baseAndDigits.length == 0) return Double.NaN;
		double numeralSystemBase = baseAndDigits[0];
		double[] digits = new double[baseAndDigits.length-1];
		System.arraycopy(baseAndDigits, 1, digits, 0, baseAndDigits.length - 1);
		return convOthBase2Decimal(numeralSystemBase, digits);
	}

	public static double numberOfDigits(double number) {
		if (Double.isNaN(number)) return Double.NaN;
		if (Double.isInfinite(number)) return Double.POSITIVE_INFINITY;
		if (number < 0.0) number = -number;
		number = MathFunctions.floor(number);
		if (number < 1.0e1) return 1;
		else if (number < 1.0e2) return 2;
		else if (number < 1.0e3) return 3;
		else if (number < 1.0e4) return 4;
		else if (number < 1.0e5) return 5;
		else if (number < 1.0e6) return 6;
		else if (number < 1.0e7) return 7;
		else if (number < 1.0e8) return 8;
		else if (number < 1.0e9) return 9;
		else if (number < 1.0e10) return 10;
		else if (number < 1.0e11) return 11;
		else if (number < 1.0e12) return 12;
		else if (number < 1.0e13) return 13;
		else if (number < 1.0e14) return 14;
		else if (number < 1.0e15) return 15;
		else if (number < 1.0e16) return 16;
		else if (number < 1.0e17) return 17;
		else if (number < 1.0e18) return 18;
		else if (number < 1.0e19) return 19;
		else if (number < 1.0e20) return 20;
		else if (number < 1.0e21) return 21;
		else if (number < 1.0e22) return 22;
		else if (number < 1.0e23) return 23;
		else if (number < 1.0e24) return 24;
		else if (number < 1.0e25) return 25;
		else if (number < 1.0e26) return 26;
		else if (number < 1.0e27) return 27;
		else if (number < 1.0e28) return 28;
		else if (number < 1.0e29) return 29;
		else if (number < 1.0e30) return 30;
		else if (number < 1.0e31) return 31;
		else if (number < 1.0e32) return 32;
		else if (number < 1.0e33) return 33;
		else if (number < 1.0e34) return 34;
		else if (number < 1.0e35) return 35;
		else if (number < 1.0e36) return 36;
		else if (number < 1.0e37) return 37;
		else if (number < 1.0e38) return 38;
		else if (number < 1.0e39) return 39;
		else if (number < 1.0e40) return 40;
		else if (number < 1.0e41) return 41;
		else if (number < 1.0e42) return 42;
		else if (number < 1.0e43) return 43;
		else if (number < 1.0e44) return 44;
		else if (number < 1.0e45) return 45;
		else if (number < 1.0e46) return 46;
		else if (number < 1.0e47) return 47;
		else if (number < 1.0e48) return 48;
		else if (number < 1.0e49) return 49;
		else if (number < 1.0e50) return 50;
		else if (number < 1.0e51) return 51;
		else if (number < 1.0e52) return 52;
		else if (number < 1.0e53) return 53;
		else if (number < 1.0e54) return 54;
		else if (number < 1.0e55) return 55;
		else if (number < 1.0e56) return 56;
		else if (number < 1.0e57) return 57;
		else if (number < 1.0e58) return 58;
		else if (number < 1.0e59) return 59;
		else if (number < 1.0e60) return 60;
		else if (number < 1.0e61) return 61;
		else if (number < 1.0e62) return 62;
		else if (number < 1.0e63) return 63;
		else if (number < 1.0e64) return 64;
		else if (number < 1.0e65) return 65;
		else if (number < 1.0e66) return 66;
		else if (number < 1.0e67) return 67;
		else if (number < 1.0e68) return 68;
		else if (number < 1.0e69) return 69;
		else if (number < 1.0e70) return 70;
		else if (number < 1.0e71) return 71;
		else if (number < 1.0e72) return 72;
		else if (number < 1.0e73) return 73;
		else if (number < 1.0e74) return 74;
		else if (number < 1.0e75) return 75;
		else if (number < 1.0e76) return 76;
		else if (number < 1.0e77) return 77;
		else if (number < 1.0e78) return 78;
		else if (number < 1.0e79) return 79;
		else if (number < 1.0e80) return 80;
		else if (number < 1.0e81) return 81;
		else if (number < 1.0e82) return 82;
		else if (number < 1.0e83) return 83;
		else if (number < 1.0e84) return 84;
		else if (number < 1.0e85) return 85;
		else if (number < 1.0e86) return 86;
		else if (number < 1.0e87) return 87;
		else if (number < 1.0e88) return 88;
		else if (number < 1.0e89) return 89;
		else if (number < 1.0e90) return 90;
		else if (number < 1.0e91) return 91;
		else if (number < 1.0e92) return 92;
		else if (number < 1.0e93) return 93;
		else if (number < 1.0e94) return 94;
		else if (number < 1.0e95) return 95;
		else if (number < 1.0e96) return 96;
		else if (number < 1.0e97) return 97;
		else if (number < 1.0e98) return 98;
		else if (number < 1.0e99) return 99;
		else if (number < 1.0e100) return 100;
		else if (number < 1.0e101) return 101;
		else if (number < 1.0e102) return 102;
		else if (number < 1.0e103) return 103;
		else if (number < 1.0e104) return 104;
		else if (number < 1.0e105) return 105;
		else if (number < 1.0e106) return 106;
		else if (number < 1.0e107) return 107;
		else if (number < 1.0e108) return 108;
		else if (number < 1.0e109) return 109;
		else if (number < 1.0e110) return 110;
		else if (number < 1.0e111) return 111;
		else if (number < 1.0e112) return 112;
		else if (number < 1.0e113) return 113;
		else if (number < 1.0e114) return 114;
		else if (number < 1.0e115) return 115;
		else if (number < 1.0e116) return 116;
		else if (number < 1.0e117) return 117;
		else if (number < 1.0e118) return 118;
		else if (number < 1.0e119) return 119;
		else if (number < 1.0e120) return 120;
		else if (number < 1.0e121) return 121;
		else if (number < 1.0e122) return 122;
		else if (number < 1.0e123) return 123;
		else if (number < 1.0e124) return 124;
		else if (number < 1.0e125) return 125;
		else if (number < 1.0e126) return 126;
		else if (number < 1.0e127) return 127;
		else if (number < 1.0e128) return 128;
		else if (number < 1.0e129) return 129;
		else if (number < 1.0e130) return 130;
		else if (number < 1.0e131) return 131;
		else if (number < 1.0e132) return 132;
		else if (number < 1.0e133) return 133;
		else if (number < 1.0e134) return 134;
		else if (number < 1.0e135) return 135;
		else if (number < 1.0e136) return 136;
		else if (number < 1.0e137) return 137;
		else if (number < 1.0e138) return 138;
		else if (number < 1.0e139) return 139;
		else if (number < 1.0e140) return 140;
		else if (number < 1.0e141) return 141;
		else if (number < 1.0e142) return 142;
		else if (number < 1.0e143) return 143;
		else if (number < 1.0e144) return 144;
		else if (number < 1.0e145) return 145;
		else if (number < 1.0e146) return 146;
		else if (number < 1.0e147) return 147;
		else if (number < 1.0e148) return 148;
		else if (number < 1.0e149) return 149;
		else if (number < 1.0e150) return 150;
		else if (number < 1.0e151) return 151;
		else if (number < 1.0e152) return 152;
		else if (number < 1.0e153) return 153;
		else if (number < 1.0e154) return 154;
		else if (number < 1.0e155) return 155;
		else if (number < 1.0e156) return 156;
		else if (number < 1.0e157) return 157;
		else if (number < 1.0e158) return 158;
		else if (number < 1.0e159) return 159;
		else if (number < 1.0e160) return 160;
		else if (number < 1.0e161) return 161;
		else if (number < 1.0e162) return 162;
		else if (number < 1.0e163) return 163;
		else if (number < 1.0e164) return 164;
		else if (number < 1.0e165) return 165;
		else if (number < 1.0e166) return 166;
		else if (number < 1.0e167) return 167;
		else if (number < 1.0e168) return 168;
		else if (number < 1.0e169) return 169;
		else if (number < 1.0e170) return 170;
		else if (number < 1.0e171) return 171;
		else if (number < 1.0e172) return 172;
		else if (number < 1.0e173) return 173;
		else if (number < 1.0e174) return 174;
		else if (number < 1.0e175) return 175;
		else if (number < 1.0e176) return 176;
		else if (number < 1.0e177) return 177;
		else if (number < 1.0e178) return 178;
		else if (number < 1.0e179) return 179;
		else if (number < 1.0e180) return 180;
		else if (number < 1.0e181) return 181;
		else if (number < 1.0e182) return 182;
		else if (number < 1.0e183) return 183;
		else if (number < 1.0e184) return 184;
		else if (number < 1.0e185) return 185;
		else if (number < 1.0e186) return 186;
		else if (number < 1.0e187) return 187;
		else if (number < 1.0e188) return 188;
		else if (number < 1.0e189) return 189;
		else if (number < 1.0e190) return 190;
		else if (number < 1.0e191) return 191;
		else if (number < 1.0e192) return 192;
		else if (number < 1.0e193) return 193;
		else if (number < 1.0e194) return 194;
		else if (number < 1.0e195) return 195;
		else if (number < 1.0e196) return 196;
		else if (number < 1.0e197) return 197;
		else if (number < 1.0e198) return 198;
		else if (number < 1.0e199) return 199;
		else if (number < 1.0e200) return 200;
		else if (number < 1.0e201) return 201;
		else if (number < 1.0e202) return 202;
		else if (number < 1.0e203) return 203;
		else if (number < 1.0e204) return 204;
		else if (number < 1.0e205) return 205;
		else if (number < 1.0e206) return 206;
		else if (number < 1.0e207) return 207;
		else if (number < 1.0e208) return 208;
		else if (number < 1.0e209) return 209;
		else if (number < 1.0e210) return 210;
		else if (number < 1.0e211) return 211;
		else if (number < 1.0e212) return 212;
		else if (number < 1.0e213) return 213;
		else if (number < 1.0e214) return 214;
		else if (number < 1.0e215) return 215;
		else if (number < 1.0e216) return 216;
		else if (number < 1.0e217) return 217;
		else if (number < 1.0e218) return 218;
		else if (number < 1.0e219) return 219;
		else if (number < 1.0e220) return 220;
		else if (number < 1.0e221) return 221;
		else if (number < 1.0e222) return 222;
		else if (number < 1.0e223) return 223;
		else if (number < 1.0e224) return 224;
		else if (number < 1.0e225) return 225;
		else if (number < 1.0e226) return 226;
		else if (number < 1.0e227) return 227;
		else if (number < 1.0e228) return 228;
		else if (number < 1.0e229) return 229;
		else if (number < 1.0e230) return 230;
		else if (number < 1.0e231) return 231;
		else if (number < 1.0e232) return 232;
		else if (number < 1.0e233) return 233;
		else if (number < 1.0e234) return 234;
		else if (number < 1.0e235) return 235;
		else if (number < 1.0e236) return 236;
		else if (number < 1.0e237) return 237;
		else if (number < 1.0e238) return 238;
		else if (number < 1.0e239) return 239;
		else if (number < 1.0e240) return 240;
		else if (number < 1.0e241) return 241;
		else if (number < 1.0e242) return 242;
		else if (number < 1.0e243) return 243;
		else if (number < 1.0e244) return 244;
		else if (number < 1.0e245) return 245;
		else if (number < 1.0e246) return 246;
		else if (number < 1.0e247) return 247;
		else if (number < 1.0e248) return 248;
		else if (number < 1.0e249) return 249;
		else if (number < 1.0e250) return 250;
		else if (number < 1.0e251) return 251;
		else if (number < 1.0e252) return 252;
		else if (number < 1.0e253) return 253;
		else if (number < 1.0e254) return 254;
		else if (number < 1.0e255) return 255;
		else if (number < 1.0e256) return 256;
		else if (number < 1.0e257) return 257;
		else if (number < 1.0e258) return 258;
		else if (number < 1.0e259) return 259;
		else if (number < 1.0e260) return 260;
		else if (number < 1.0e261) return 261;
		else if (number < 1.0e262) return 262;
		else if (number < 1.0e263) return 263;
		else if (number < 1.0e264) return 264;
		else if (number < 1.0e265) return 265;
		else if (number < 1.0e266) return 266;
		else if (number < 1.0e267) return 267;
		else if (number < 1.0e268) return 268;
		else if (number < 1.0e269) return 269;
		else if (number < 1.0e270) return 270;
		else if (number < 1.0e271) return 271;
		else if (number < 1.0e272) return 272;
		else if (number < 1.0e273) return 273;
		else if (number < 1.0e274) return 274;
		else if (number < 1.0e275) return 275;
		else if (number < 1.0e276) return 276;
		else if (number < 1.0e277) return 277;
		else if (number < 1.0e278) return 278;
		else if (number < 1.0e279) return 279;
		else if (number < 1.0e280) return 280;
		else if (number < 1.0e281) return 281;
		else if (number < 1.0e282) return 282;
		else if (number < 1.0e283) return 283;
		else if (number < 1.0e284) return 284;
		else if (number < 1.0e285) return 285;
		else if (number < 1.0e286) return 286;
		else if (number < 1.0e287) return 287;
		else if (number < 1.0e288) return 288;
		else if (number < 1.0e289) return 289;
		else if (number < 1.0e290) return 290;
		else if (number < 1.0e291) return 291;
		else if (number < 1.0e292) return 292;
		else if (number < 1.0e293) return 293;
		else if (number < 1.0e294) return 294;
		else if (number < 1.0e295) return 295;
		else if (number < 1.0e296) return 296;
		else if (number < 1.0e297) return 297;
		else if (number < 1.0e298) return 298;
		else if (number < 1.0e299) return 299;
		else if (number < 1.0e300) return 300;
		else if (number < 1.0e301) return 301;
		else if (number < 1.0e302) return 302;
		else if (number < 1.0e303) return 303;
		else if (number < 1.0e304) return 304;
		else if (number < 1.0e305) return 305;
		else if (number < 1.0e306) return 306;
		else if (number < 1.0e307) return 307;
		else if (number < 1.0e308) return 308;
		else return 309;
	}

	public static double numberOfDigits(double number, double numeralSystemBase) {
		if (Double.isNaN(number)) return Double.NaN;
		if (Double.isNaN(numeralSystemBase)) return Double.NaN;
		if (Double.isInfinite(numeralSystemBase)) return Double.NaN;
		if (numeralSystemBase < 1.0) return Double.NaN;
		if (Double.isInfinite(number)) return Double.POSITIVE_INFINITY;
		if (number < 0.0) number = -number;
		number = MathFunctions.floor(number);
		numeralSystemBase = MathFunctions.floor(numeralSystemBase);
		if (numeralSystemBase == 10.0) return numberOfDigits(number);
		if (numeralSystemBase == 1.0) return (int)number;
		if (number < numeralSystemBase) return 1;
		double quotient = number;
		double digitsNum = 0;
		while (quotient >= 1.0) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			quotient = MathFunctions.floor(quotient / numeralSystemBase);
			digitsNum++;
		}
		return digitsNum;
	}

	public static double digitAtPosition(double number, double position, double numeralSystemBase) {
		if (Double.isNaN(number)) return Double.NaN;
		if (Double.isNaN(position)) return Double.NaN;
		if (Double.isNaN(numeralSystemBase)) return Double.NaN;
		if (Double.isInfinite(number)) return Double.NaN;
		if (Double.isInfinite(position)) return Double.NaN;
		if (Double.isInfinite(numeralSystemBase)) return Double.NaN;
		if (numeralSystemBase < 1.0) return Double.NaN;
		if (number < 0) number = -number;
		number = MathFunctions.floor(number);
		numeralSystemBase = MathFunctions.floor(numeralSystemBase);
		int digitsNum = (int)numberOfDigits(number, numeralSystemBase);
		if (position <= -digitsNum) {
			if (numeralSystemBase > 1.0) return 0;
			else return Double.NaN;
		}
		if (position > digitsNum) return Double.NaN;
		if (numeralSystemBase == 1.0) return 1.0;
		double[] digits = new double[digitsNum];
		double quotient = number;
		double digit;
		int digitIndex = digitsNum;
		while (quotient >= 1.0) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			digit = MathFunctions.floor(quotient % numeralSystemBase);
			quotient = MathFunctions.floor(quotient / numeralSystemBase);
			digitIndex--;
			digits[digitIndex] = digit;
		}
		if (position >= 1) return digits[(int)(position-1)];
		else return digits[(int)(digitsNum+position-1)];
	}
	public static double digitAtPosition(double number, double position) {
		return digitAtPosition(number, position, 10.0);
	}
	public static double[] primeFactors(double number) {
		double[] doubleZeroArray = new double[0];
		double[] factors;
		if (Double.isNaN(number)) return doubleZeroArray;
		if (Double.isInfinite(number)) return doubleZeroArray;
		number = MathFunctions.floor( MathFunctions.abs(number) );
		if (number == 0.0) return doubleZeroArray;
		if (number == 1.0) {
			factors = new double[1];
			factors[0] = 1.0;
			return factors;
		}
		if (mXparser.primesCache != null)
			if (mXparser.primesCache.getCacheStatus() == PrimesCache.CACHING_FINISHED)
				if (number <= Integer.MAX_VALUE)
					if ( mXparser.primesCache.primeTest((int)number) == PrimesCache.IS_PRIME ) {
						factors = new double[1];
						factors[0] = number;
						return factors;
					}
		double n = number;
		List<Double> factorsList = new ArrayList<>();
		for (double i = 2.0; i <= MathFunctions.floor(n / i); MathFunctions.floor(i++)) {
			while (n % i == 0) {
				factorsList.add(i);
				n = MathFunctions.floor(n / i);
			}
		}
		if (n > 1.0) factorsList.add(n);
		int nfact = factorsList.size();
		factors = new double[nfact];
		for (int i = 0; i < nfact; i++)
			factors[i] = factorsList.get(i);
        return factors;
	}
	public static double numberOfPrimeFactors(double number) {
		if (Double.isNaN(number)) return Double.NaN;
		double[] factors = primeFactors(number);
		if (factors.length <= 1) return factors.length;
		double[][] factorsDist = NumberTheory.getDistValues(factors, false);
		return factorsDist.length;
	}
	public static double primeFactorValue(double number, double id) {
		if (Double.isNaN(number)) return Double.NaN;
		if (Double.isNaN(id)) return Double.NaN;
		if (Double.isInfinite(number)) return Double.NaN;
		if (Double.isInfinite(id)) return Double.NaN;
		number = MathFunctions.floor( MathFunctions.abs(number) );
		if (number == 0.0) return Double.NaN;
		if (id < 1) return 1;
		id = MathFunctions.floor(id);
		if (id > Integer.MAX_VALUE) return 1;
		double[] factors = primeFactors(number);
		double[][] factorsDist = NumberTheory.getDistValues(factors, false);
		int nfact = factorsDist.length;
		if (id > nfact) return 1;
		return factorsDist[(int)(id-1)][0];
	}
	public static double primeFactorExponent(double number, double id) {
		if (Double.isNaN(number)) return Double.NaN;
		if (Double.isNaN(id)) return Double.NaN;
		if (Double.isInfinite(number)) return Double.NaN;
		if (Double.isInfinite(id)) return Double.NaN;
		number = MathFunctions.floor( MathFunctions.abs(number) );
		if (number == 0.0) return Double.NaN;
		if (id < 1) return 0;
		id = MathFunctions.floor(id);
		if (id > Integer.MAX_VALUE) return 0;
		double[] factors = primeFactors(number);
		double[][] factorsDist = NumberTheory.getDistValues(factors, false);
		int nfact = factorsDist.length;
		if (id > nfact) return 0;
		return factorsDist[(int)(id-1)][1];
	}

}