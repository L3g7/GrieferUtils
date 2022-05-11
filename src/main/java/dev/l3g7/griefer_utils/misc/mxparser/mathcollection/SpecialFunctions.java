package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;

public final class SpecialFunctions {

	public static double exponentialIntegralEi(double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		if (x < -5.0)
			return continuedFractionEi(x);
		if (x == 0.0)
			return -Double.MAX_VALUE;
		if (x < 6.8)
			return powerSeriesEi(x);
		if (x < 50.0)
			return argumentAdditionSeriesEi(x);
		return continuedFractionEi(x);
	}
	private static final double EI_DBL_EPSILON = Math.ulp(1.0);
	private static final double EI_EPSILON = 10.0 * EI_DBL_EPSILON;
	private static double continuedFractionEi(double x) {
		double Am1 = 1.0;
		double A0 = 0.0;
		double Bm1 = 0.0;
		double B0 = 1.0;
		double a = Math.exp(x);
		double b = -x + 1.0;
		double Ap1 = b * A0 + a * Am1;
		double Bp1 = b * B0 + a * Bm1;
		int j = 1;
		while (Math.abs(Ap1 * B0 - A0 * Bp1) > EI_EPSILON * Math.abs(A0 * Bp1)) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			if (Math.abs(Bp1) > 1.0) {
				Am1 = A0 / Bp1;
				A0 = Ap1 / Bp1;
				Bm1 = B0 / Bp1;
				B0 = 1.0;
			} else {
				Am1 = A0;
				A0 = Ap1;
				Bm1 = B0;
				B0 = Bp1;
			}
			a = -j * j;
			b += 2.0;
			Ap1 = b * A0 + a * Am1;
			Bp1 = b * B0 + a * Bm1;
			j += 1;
		}
		return (-Ap1 / Bp1);
	}
	private static double powerSeriesEi(double x) {
		double xn = -x;
		double Sn = -x;
		double Sm1 = 0.0;
		double hsum = 1.0;
		final double g = MathConstants.EULER_MASCHERONI;
		double y = 1.0;
		double factorial = 1.0;
		if (x == 0.0)
			return -Double.MAX_VALUE;
		while (Math.abs(Sn - Sm1) > EI_EPSILON * Math.abs(Sm1)) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			Sm1 = Sn;
			y += 1.0;
			xn *= (-x);
			factorial *= y;
			hsum += (1.0 / y);
			Sn += hsum * xn / factorial;
		}
		return (g + Math.log(Math.abs(x)) - Math.exp(x) * Sn);
	}

	private static double argumentAdditionSeriesEi(double x) {
		final int k = (int) (x + 0.5);
		int j = 0;
		final double dx = x - (double) k;
		double xxj = k;
		final double edx = Math.exp(dx);
		double Sm = 1.0;
		double Sn = (edx - 1.0) / xxj;
		double term = Double.MAX_VALUE;
		double factorial = 1.0;
		double dxj = 1.0;
		while (Math.abs(term) > EI_EPSILON * Math.abs(Sn)) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			j++;
			factorial *= j;
			xxj *= k;
			dxj *= (-dx);
			Sm += (dxj / factorial);
			term = (factorial * (edx * Sm - 1.0)) / xxj;
			Sn += term;
		}
		return Coefficients.EI[k - 7] + Sn * Math.exp(k);
	}
	public static double logarithmicIntegralLi(double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		if (x < 0)
			return Double.NaN;
		if (x == 0)
			return 0;
		if (x == 2)
			return MathConstants.LI2;
		return exponentialIntegralEi( MathFunctions.ln(x) );
	}
	public static double offsetLogarithmicIntegralLi(double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		if (x < 0)
			return Double.NaN;
		if (x == 0)
			return -MathConstants.LI2;
		return logarithmicIntegralLi(x) - MathConstants.LI2;
	}
	public static double erf(double x) {
		if (Double.isNaN(x)) return Double.NaN;
		if (x == 0) return 0;
		if (x == Double.POSITIVE_INFINITY) return 1.0;
		if (x == Double.NEGATIVE_INFINITY) return -1.0;
		return erfImp(x, false);
	}
	public static double erfc(double x) {
		if (Double.isNaN(x)) return Double.NaN;
		if (x == 0) return 1;
		if (x == Double.POSITIVE_INFINITY) return 0.0;
		if (x == Double.NEGATIVE_INFINITY) return 2.0;
		return erfImp(x, true);
	}
	public static double erfInv(double x) {
		if (x == 0.0) return 0;
		if (x >= 1.0) return Double.POSITIVE_INFINITY;
		if (x <= -1.0) return Double.NEGATIVE_INFINITY;
		double p, q, s;
		if (x < 0) {
			p = -x;
			q = 1 - p;
			s = -1;
		} else {
			p = x;
			q = 1 - x;
			s = 1;
		}
		return erfInvImpl(p, q, s);
	}
	private static double erfImp(double z, boolean invert) {
    	if (z < 0) {
        	if (!invert) return -erfImp(-z, false);
        	if (z < -0.5) return 2 - erfImp(-z, true);
            return 1 + erfImp(-z, false);
        }
    	double result;
    	if (z < 0.5) {
    		if (z < 1e-10) result = (z*1.125) + (z*0.003379167095512573896158903121545171688);
    		else result = (z*1.125) + (z*Evaluate.polynomial(z, Coefficients.erfImpAn) / Evaluate.polynomial(z, Coefficients.erfImpAd));
    	}
    	else if (z < 110) {
    		invert = !invert;
    		double r, b;
    		if(z < 0.75) {
    			r = Evaluate.polynomial(z - 0.5, Coefficients.erfImpBn) / Evaluate.polynomial(z - 0.5, Coefficients.erfImpBd);
    			b = 0.3440242112F;
    		}
    		else if (z < 1.25) {
    			r = Evaluate.polynomial(z - 0.75, Coefficients.erfImpCn) / Evaluate.polynomial(z - 0.75, Coefficients.erfImpCd);
    			b = 0.419990927F;
    		} else if (z < 2.25) {
    			r = Evaluate.polynomial(z - 1.25, Coefficients.erfImpDn) / Evaluate.polynomial(z - 1.25, Coefficients.erfImpDd);
    			b = 0.4898625016F;
    		} else if (z < 3.5) {
    			r = Evaluate.polynomial(z - 2.25, Coefficients.erfImpEn) / Evaluate.polynomial(z - 2.25, Coefficients.erfImpEd);
    			b = 0.5317370892F;
    		} else if (z < 5.25) {
    			r = Evaluate.polynomial(z - 3.5, Coefficients.erfImpFn) / Evaluate.polynomial(z - 3.5, Coefficients.erfImpFd);
    			b = 0.5489973426F;
    		} else if (z < 8) {
    			r = Evaluate.polynomial(z - 5.25, Coefficients.erfImpGn) / Evaluate.polynomial(z - 5.25, Coefficients.erfImpGd);
    			b = 0.5571740866F;
    		} else if (z < 11.5) {
    			r = Evaluate.polynomial(z - 8, Coefficients.erfImpHn) / Evaluate.polynomial(z - 8, Coefficients.erfImpHd);
    			b = 0.5609807968F;
    		} else if (z < 17) {
    			r = Evaluate.polynomial(z - 11.5, Coefficients.erfImpIn) / Evaluate.polynomial(z - 11.5, Coefficients.erfImpId);
    			b = 0.5626493692F;
    		} else if (z < 24) {
    			r = Evaluate.polynomial(z - 17, Coefficients.erfImpJn) / Evaluate.polynomial(z - 17, Coefficients.erfImpJd);
    			b = 0.5634598136F;
    		} else if (z < 38) {
    			r = Evaluate.polynomial(z - 24, Coefficients.erfImpKn) / Evaluate.polynomial(z - 24, Coefficients.erfImpKd);
    			b = 0.5638477802F;
    		} else if (z < 60) {
    			r = Evaluate.polynomial(z - 38, Coefficients.erfImpLn) / Evaluate.polynomial(z - 38, Coefficients.erfImpLd);
    			b = 0.5640528202F;
    		} else if (z < 85) {
    			r = Evaluate.polynomial(z - 60, Coefficients.erfImpMn) / Evaluate.polynomial(z - 60, Coefficients.erfImpMd);
    			b = 0.5641309023F;
    		} else {
    			r = Evaluate.polynomial(z - 85, Coefficients.erfImpNn) / Evaluate.polynomial(z - 85, Coefficients.erfImpNd);
    			b = 0.5641584396F;
    		}
    		double g = MathFunctions.exp(-z*z)/z;
    		result = (g*b) + (g*r);
    	} else {
    		result = 0;
    		invert = !invert;
    	}
        if (invert) result = 1 - result;
        return result;
	}
	public static double erfcInv(double z) {
		if (z <= 0.0) return Double.POSITIVE_INFINITY;
        if (z >= 2.0) return Double.NEGATIVE_INFINITY;
        double p, q, s;
        if (z > 1) {
        	q = 2 - z;
        	p = 1 - q;
        	s = -1;
        } else {
        	p = 1 - z;
        	q = z;
        	s = 1;
        }
        return erfInvImpl(p, q, s);
	}
	private static double erfInvImpl(double p, double q, double s) {
    	double result;
    	if (p <= 0.5) {
    		final float y = 0.0891314744949340820313f;
    		double g = p*(p + 10);
    		double r = Evaluate.polynomial(p, Coefficients.ervInvImpAn) / Evaluate.polynomial(p, Coefficients.ervInvImpAd);
    		result = (g*y) + (g*r);
    	} else if (q >= 0.25) {
    		final float y = 2.249481201171875f;
    		double g = MathFunctions.sqrt(-2 * MathFunctions.ln(q));
    		double xs = q - 0.25;
    		double r = Evaluate.polynomial(xs, Coefficients.ervInvImpBn) / Evaluate.polynomial(xs, Coefficients.ervInvImpBd);
    		result = g/(y + r);
    	} else {
    		double x = MathFunctions.sqrt(-MathFunctions.ln(q));
    		if (x < 3) {
    			final float y = 0.807220458984375f;
                double xs = x - 1.125;
                double r = Evaluate.polynomial(xs, Coefficients.ervInvImpCn) / Evaluate.polynomial(xs, Coefficients.ervInvImpCd);
                result = (y*x) + (r*x);
    		} else if (x < 6) {
    			final float y = 0.93995571136474609375f;
    			double xs = x - 3;
    			double r = Evaluate.polynomial(xs, Coefficients.ervInvImpDn) / Evaluate.polynomial(xs, Coefficients.ervInvImpDd);
    			result = (y*x) + (r*x);
    		} else if (x < 18) {
    			final float y = 0.98362827301025390625f;
    			double xs = x - 6;
    			double r = Evaluate.polynomial(xs, Coefficients.ervInvImpEn) / Evaluate.polynomial(xs, Coefficients.ervInvImpEd);
    			result = (y*x) + (r*x);
    		} else if (x < 44) {
    			final float y = 0.99714565277099609375f;
    			double xs = x - 18;
    			double r = Evaluate.polynomial(xs, Coefficients.ervInvImpFn) / Evaluate.polynomial(xs, Coefficients.ervInvImpFd);
    			result = (y*x) + (r*x);
            } else {
            	final float y = 0.99941349029541015625f;
            	double xs = x - 44;
            	double r = Evaluate.polynomial(xs, Coefficients.ervInvImpGn) / Evaluate.polynomial(xs, Coefficients.ervInvImpGd);
            	result = (y*x) + (r*x);
            }
    	}
    	return s*result;
	}
	private static double gammaInt(long n) {
		if (n == 0) return MathConstants.EULER_MASCHERONI;
		if (n == 1) return 1;
		if (n == 2) return 1;
		if (n == 3) return 2.0;
		if (n == 4) return 1.0*2.0*3.0;
		if (n == 5) return 1.0*2.0*3.0*4.0;
		if (n == 6) return 1.0*2.0*3.0*4.0*5.0;
		if (n == 7) return 1.0*2.0*3.0*4.0*5.0*6.0;
		if (n == 8) return 1.0*2.0*3.0*4.0*5.0*6.0*7.0;
		if (n == 9) return 1.0*2.0*3.0*4.0*5.0*6.0*7.0*8.0;
		if (n == 10) return 1.0*2.0*3.0*4.0*5.0*6.0*7.0*8.0*9.0;
		if (n >= 11) return MathFunctions.factorial(n-1);
		long r = -n;
		double factr = MathFunctions.factorial(r);
		double sign = -1;
		if (r % 2 == 0) sign = 1;
		return sign / (r * factr) - (1.0 / r) * gammaInt(n + 1);
	}
	public static double gamma(double d) {
		if (Double.isNaN(d)) return Double.NaN;
		if (d == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
		if (d == Double.NEGATIVE_INFINITY) return Double.NaN;
		double xabs = MathFunctions.abs(d);
		double xint = Math.round(xabs);
		if ( MathFunctions.abs(xabs-xint) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON ) {
			long n = (long)xint;
			if (d < 0) n = -n;
			return gammaInt(n);
		}
		return lanchosGamma(d);
	}
	public static double lanchosGamma(double x) {
		if (Double.isNaN(x)) return Double.NaN;

		double xabs = MathFunctions.abs(x);
		double xint = Math.round(xabs);
		if (x > BinaryRelations.DEFAULT_COMPARISON_EPSILON) {
			if ( MathFunctions.abs(xabs-xint) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON )
				return MathFunctions.factorial(xint-1);
		} else if (x < -BinaryRelations.DEFAULT_COMPARISON_EPSILON) {
			if ( MathFunctions.abs(xabs-xint) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON )
				return Double.NaN;
		} else return Double.NaN;
		if(x < 0.5) return MathConstants.PI / (Math.sin(MathConstants.PI * x) * lanchosGamma(1-x));
		int g = 7;
		x -= 1;
		double a = Coefficients.lanchosGamma[0];
		double t = x+g+0.5;
		for(int i = 1; i < Coefficients.lanchosGamma.length; i++){
			a += Coefficients.lanchosGamma[i] / (x+i);
		}
		return Math.sqrt(2*MathConstants.PI) * Math.pow(t, x+0.5) * Math.exp(-t) * a;
	}
	public static double logGamma(double d) {
		if (Double.isNaN(d)) return Double.NaN;
		if (d == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
		if (d == Double.NEGATIVE_INFINITY) return Double.NaN;
		if (MathFunctions.isInteger(d)) {
			if (d >= 0)
				return Math.log( Math.abs( gammaInt(Math.round(d)) ) );
			else
				return Math.log( Math.abs( gammaInt( -(long)(Math.round(-d) ) ) ) );
		}
		double p, q, w, z;
		if (d < -34.0) {
			q = -d;
			w = logGamma(q);
			p = Math.floor(q);
			if (Math.abs(p-q) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
			z = q - p;
			if (z > 0.5) {
				p += 1.0;
				z = p - q;
			}
			z = q * Math.sin( Math.PI * z );
			if (Math.abs(z) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
			z = MathConstants.LNPI - Math.log(z) - w;
			return z;
		}
		if (d < 13.0) {
			z = 1.0;
			while (d >= 3.0) {
				d -= 1.0;
				z *= d;
			}
			while (d < 2.0) {
				if( Math.abs(d) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON ) return Double.NaN;
				z /= d;
				d += 1.0;
			}
			if (z < 0.0) z = -z;
			if (d == 2.0) return Math.log(z);
			d -= 2.0;
			p = d * Evaluate.polevl( d, Coefficients.logGammaB, 5 ) / Evaluate.p1evl( d, Coefficients.logGammaC, 6);
			return Math.log(z) + p;
		}
		if (d > 2.556348e305) return Double.NaN;
		q = (d - 0.5) * Math.log(d) - d + 0.91893853320467274178;
		if (d > 1.0e8) return q;
		p = 1.0/(d*d);
		if (d >= 1000.0)
			q += (( 7.9365079365079365079365e-4 * p - 2.7777777777777777777778e-3 ) * p + 0.0833333333333333333333 ) / d;
		else
			q += Evaluate.polevl( p, Coefficients.logGammaA, 4 ) / d;
		return q;
	}
	public static double sgnGamma(double x) {
		if (Double.isNaN(x)) return Double.NaN;
		if (x == Double.POSITIVE_INFINITY) return 1;
		if (x == Double.NEGATIVE_INFINITY) return Double.NaN;
		if (x > 0) return 1;
		if (MathFunctions.isInteger(x)) return MathFunctions.sgn( gammaInt( -(long)(Math.round(-x) ) ) );
		x = -x;
		double fx = Math.floor(x);
		double div2remainder = Math.floor(fx % 2);
		if (div2remainder == 0) return -1;
		else return 1;
	}
	public static double regularizedGammaLowerP(double s, double x) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(s)) return Double.NaN;
		if (MathFunctions.almostEqual(x, 0)) return 0;
		if (MathFunctions.almostEqual(s, 0))
			return 1 + SpecialFunctions.exponentialIntegralEi(-x) / MathConstants.EULER_MASCHERONI;

		if (MathFunctions.almostEqual(s, 1))
			return 1 - Math.exp(-x);

		if (x < 0) return Double.NaN;

		if (s < 0)
			return regularizedGammaLowerP(s + 1, x) + ( Math.pow(x,  s) * Math.exp(-x) ) / ( s * gamma(s) );

		final double epsilon = 0.000000000000001;
		final double bigNumber = 4503599627370496.0;
		final double bigNumberInverse = 2.22044604925031308085e-16;

		double ax = (s * Math.log(x)) - x - logGamma(s);
		if (ax < -709.78271289338399) {
			return 1;
		}

		if (x <= 1 || x <= s) {
			double r2 = s;
			double c2 = 1;
			double ans2 = 1;
			do {
				r2 = r2 + 1;
				c2 = c2 * x / r2;
				ans2 += c2;
			} while ((c2 / ans2) > epsilon);
			return Math.exp(ax) * ans2 / s;
        }

		int c = 0;
		double y = 1 - s;
		double z = x + y + 1;

		double p3 = 1;
		double q3 = x;
		double p2 = x + 1;
		double q2 = z * x;
		double ans = p2 / q2;

		double error;

        do {
        	if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
        	c++;
        	y += 1;
        	z += 2;
        	double yc = y * c;

        	double p = (p2 * z) - (p3 * yc);
        	double q = (q2 * z) - (q3 * yc);

        	if (q != 0) {
        		double nextans = p / q;
        		error = Math.abs((ans - nextans) / nextans);
        		ans = nextans;
        	} else {
        		// zero div, skip
        		error = 1;
        	}

        	// shift
        	p3 = p2;
        	p2 = p;
        	q3 = q2;
        	q2 = q;

        	// normalize fraction when the numerator becomes large
        	if (Math.abs(p) > bigNumber) {
        		p3 *= bigNumberInverse;
        		p2 *= bigNumberInverse;
        		q3 *= bigNumberInverse;
        		q2 *= bigNumberInverse;
        	}
        } while (error > epsilon);

        return 1 - (Math.exp(ax) * ans);
  	}
	public static double incompleteGammaLower(double s, double x) {
		return gamma(s) * regularizedGammaLowerP(s, x);
	}
	public static double regularizedGammaUpperQ(double s, double x) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(s)) return Double.NaN;
		if (MathFunctions.almostEqual(x, 0)) return 1;

		if (MathFunctions.almostEqual(s, 0))
			return -SpecialFunctions.exponentialIntegralEi(-x) / MathConstants.EULER_MASCHERONI;

		if (MathFunctions.almostEqual(s, 1))
			return Math.exp(-x);

		if (x < 0) return Double.NaN;

		if (s < 0)
			return regularizedGammaUpperQ(s + 1, x) - ( Math.pow(x,  s) * Math.exp(-x) ) / ( s * gamma(s) );

        double ax = s * Math.log(x) - x - logGamma(s);
        if (ax < -709.78271289338399) {
        	return 0;
        }
		double t;
		final double igammaepsilon = 0.000000000000001;
		final double igammabignumber = 4503599627370496.0;
		final double igammabignumberinv = 2.22044604925031308085 * 0.0000000000000001;

        ax = Math.exp(ax);
        double y = 1 - s;
        double z = x + y + 1;
        double c = 0;
        double pkm2 = 1;
        double qkm2 = x;
        double pkm1 = x + 1;
        double qkm1 = z * x;
        double ans = pkm1 / qkm1;
        do {
        	if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
        	c = c + 1;
        	y = y + 1;
        	z = z + 2;
        	double yc = y * c;
        	double pk = pkm1 * z - pkm2 * yc;
        	double qk = qkm1 * z - qkm2 * yc;
        	if (qk != 0) {
        		double r = pk / qk;
        		t = Math.abs((ans - r) / r);
        		ans = r;
        	} else {
        		t = 1;
        	}

        	pkm2 = pkm1;
        	pkm1 = pk;
        	qkm2 = qkm1;
        	qkm1 = qk;

        	if (Math.abs(pk) > igammabignumber) {
        		pkm2 = pkm2 * igammabignumberinv;
        		pkm1 = pkm1 * igammabignumberinv;
        		qkm2 = qkm2 * igammabignumberinv;
        		qkm1 = qkm1 * igammabignumberinv;
        	}
        } while (t > igammaepsilon);
        return ans * ax;
	}
	public static double incompleteGammaUpper(double s, double x) {
		return gamma(s) * regularizedGammaUpperQ(s, x);
	}
	public static double diGamma(double x) {
		final double c = 12.0;
		final double d1 = -0.57721566490153286;
		final double d2 = 1.6449340668482264365;
		final double s = 1e-6;
		final double s3 = 1.0/12.0;
		final double s4 = 1.0/120.0;
		final double s5 = 1.0/252.0;
		final double s6 = 1.0/240.0;
		final double s7 = 1.0/132.0;

		if (Double.isNaN(x)) return Double.NaN;
		if (x == Double.NEGATIVE_INFINITY) return Double.NaN;
		if (x <= 0)
			if (MathFunctions.isInteger(x))
				return Double.NaN;

		// Use inversion formula for negative numbers.
		if (x < 0) return diGamma(1.0 - x) + (MathConstants.PI/Math.tan(-Math.PI*x));

		if (x <= s) return d1 - (1/x) + (d2*x);

		double result = 0;
		while (x < c) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			result -= 1/x;
			x++;
		}

		if (x >= c) {
			double r = 1/x;
			result += Math.log(x) - (0.5*r);
			r *= r;
			result -= r*(s3 - (r*(s4 - (r*(s5 - (r*(s6 - (r*s7))))))));
		}

		return result;
	}
	private static final int doubleWidth = 53;
	private static final double doublePrecision = Math.pow(2, -doubleWidth);

	public static double logBeta(double x, double y) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(y)) return Double.NaN;
		if ( (x <= 0) || (y <= 0) ) return Double.NaN;

		double lgx = logGamma(x);
		if (Double.isNaN(lgx)) lgx = Math.log( Math.abs( gamma(x) ) );

		double lgy = logGamma(y);
		if (Double.isNaN(lgy)) lgy = Math.log( Math.abs( gamma(y) ) );

		double lgxy = logGamma(x+y);
		if (Double.isNaN(lgy)) lgxy = Math.log( Math.abs( gamma(x+y) ) );

		if ( (!Double.isNaN(lgx)) && (!Double.isNaN(lgy)) && (!Double.isNaN(lgxy)) )
			return (lgx + lgy - lgxy);
		else return Double.NaN;
	}
	public static double beta(double x, double y) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(y)) return Double.NaN;
		if ( (x <= 0) || (y <= 0) ) return Double.NaN;
		if ( (x > 99) || (y > 99) ) return Math.exp(logBeta(x, y));
		return gamma(x)*gamma(y) / gamma(x+y);
	}
	public static double incompleteBeta(double a, double b, double x) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (Double.isNaN(x)) return Double.NaN;
		if (x < -BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if (x > 1+BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if ( (a <= 0) || (b <= 0) ) return Double.NaN;
		if (MathFunctions.almostEqual(x, 0)) return 0;
		if (MathFunctions.almostEqual(x, 1)) return beta(a, b);
		boolean aEq0 = MathFunctions.almostEqual(a, 0);
		boolean bEq0 = MathFunctions.almostEqual(b, 0);
		boolean aIsInt = MathFunctions.isInteger(a);
		boolean bIsInt = MathFunctions.isInteger(b);
		long aInt = 0, bInt = 0;
		if (aIsInt) aInt = (long)MathFunctions.integerPart(a);
		if (bIsInt) bInt = (long)MathFunctions.integerPart(b);

		long n;
		if (aEq0 && bEq0) return Math.log( x / (1-x) );
		if (aEq0 && bIsInt) {
			n = bInt;
			if (n >= 1) {
				if (n == 1) return Math.log(x);
				if (n == 2) return Math.log(x) + x;
				double v = Math.log(x);
				for (long i = 1; i <= n-1; i++)
					v -= MathFunctions.binomCoeff(n-1, i) * Math.pow(-1, i) * ( Math.pow(x, i) / i );
				return v;
			}
			if (n <= -1) {
				if (n == -1) return Math.log( x / (1-x) ) + 1/(1-x) - 1;
				if (n == -2) return Math.log( x / (1-x) ) - 1/x - 1/(2*x*x);
				double v = -Math.log(x / (1-x));
				for (long i = 1; i <= -n-1; i++)
					v -= Math.pow(x, -i) / i;
				return v;
			}
		}
		if (aIsInt && bEq0) {
			n = aInt;
			if (n >= 1) {
				if (n == 1) return -Math.log(1-x);
				if (n == 2) return -Math.log(1-x) - x;
				double v = -Math.log(1-x);
				for (long i = 1; i <= n-1; i++)
					v -= Math.pow(x, i) / i;
				return v;
			}
			if (n <= -1) {
				if (n == -1) return Math.log( x / (1-x) ) - 1/x;
				double v = -Math.log(x / (1-x));
				for (long i = 1; i <= -n; i++)
					v += Math.pow(1-x, -i) / i;
				for (long i = 1; i <= -n; i++)
					v -= Math.pow( MathFunctions.factorial(i-1) , 2) / i;
				return v;
			}
		}
		if(aIsInt) {
			n = aInt;
			if (MathFunctions.almostEqual(b, 1)) {
				if (n <= -1) return -( 1f/(-n) ) * Math.pow(x, n);
			}
		}
		return regularizedBeta(a, b, x)*beta(a, b);
	}
	public static double regularizedBeta(double a, double b, double x) {
		if (Double.isNaN(a)) return Double.NaN;
		if (Double.isNaN(b)) return Double.NaN;
		if (Double.isNaN(x)) return Double.NaN;
		if ( (a <= 0) || (b <= 0) ) return Double.NaN;
		if (x < -BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if (x > 1+BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if (MathFunctions.almostEqual(x, 0)) return 0;
		if (MathFunctions.almostEqual(x, 1)) return 1;

		double bt = (x == 0.0 || x == 1.0)
			? 0.0
			: Math.exp(logGamma(a + b) - logGamma(a) - logGamma(b) + (a*Math.log(x)) + (b*Math.log(1.0 - x)));

		boolean symmetryTransformation = x >= (a + 1.0)/(a + b + 2.0);

		double eps = doublePrecision;
		double fpmin = Math.nextUp(0.0)/eps;

		if (symmetryTransformation) {
			x = 1.0 - x;
			double swap = a;
			a = b;
			b = swap;
		}

		double qab = a + b;
		double qap = a + 1.0;
		double qam = a - 1.0;
		double c = 1.0;
		double d = 1.0 - (qab*x/qap);

		if (Math.abs(d) < fpmin) {
			d = fpmin;
		}

		d = 1.0/d;
		double h = d;

		for (int m = 1, m2 = 2; m <= 50000; m++, m2 += 2) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			double aa = m*(b - m)*x/((qam + m2)*(a + m2));
			d = 1.0 + (aa*d);

			if (Math.abs(d) < fpmin) {
				d = fpmin;
			}

			c = 1.0 + (aa/c);
			if (Math.abs(c) < fpmin) {
				c = fpmin;
			}

			d = 1.0/d;
			h *= d*c;
			aa = -(a + m)*(qab + m)*x/((a + m2)*(qap + m2));
			d = 1.0 + (aa*d);

			if (Math.abs(d) < fpmin) {
				d = fpmin;
			}

			c = 1.0 + (aa/c);

			if (Math.abs(c) < fpmin) {
				c = fpmin;
			}

			d = 1.0/d;
			double del = d*c;
			h *= del;

			if (Math.abs(del - 1.0) <= eps) {
				return symmetryTransformation ? 1.0 - (bt*h/a) : bt*h/a;
			}
		}
		return symmetryTransformation ? 1.0 - (bt*h/a) : bt*h/a;
	}

	private static double halleyIteration(double x, double wInitial) {
		double w = wInitial;
		double tol = 1;
		double t = 0, p, e;
		for (int i = 0; i < 100; i++) {
			if (mXparser.isCurrentCalculationCancelled()) return Double.NaN;
			e = Math.exp(w);
			p = w + 1.0;
			t = w * e - x;
			if (w > 0) t = (t / p) / e;
			else t /= e * p - 0.5 * (p + 1.0) * t / p;
		    w -= t;
			double GSL_DBL_EPSILON = 2.2204460492503131e-16;
			tol = GSL_DBL_EPSILON * Math.max(Math.abs(w), 1.0 / (Math.abs(p) * e));
		    if (Math.abs(t) < tol) return w;
		}
		double perc = Math.abs(t / tol);
		if (perc >= 0.5 && perc <= 1.5) return w;
		return Double.NaN;
	}
	private static double seriesEval(double r) {
		double t8 = Coefficients.lambertWqNearZero[8] + r * (Coefficients.lambertWqNearZero[9] + r * (Coefficients.lambertWqNearZero[10] + r * Coefficients.lambertWqNearZero[11]));
		double t5 = Coefficients.lambertWqNearZero[5] + r * (Coefficients.lambertWqNearZero[6] + r * (Coefficients.lambertWqNearZero[7] + r * t8));
		double t1 = Coefficients.lambertWqNearZero[1] + r * (Coefficients.lambertWqNearZero[2] + r * (Coefficients.lambertWqNearZero[3] + r * (Coefficients.lambertWqNearZero[4] + r * t5)));
		return Coefficients.lambertWqNearZero[0] + r * t1;
	}
	private static double lambertW0(double x) {
		if (Math.abs(x) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return 0;
		if (Math.abs(x + MathConstants.EXP_MINUS_1) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return -1;
		if (Math.abs(x - 1) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return MathConstants.OMEGA;
		if (Math.abs(x - MathConstants.E) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return 1;
		if (Math.abs(x + MathConstants.LN_SQRT2) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return -2 * MathConstants.LN_SQRT2;
		if (x < -MathConstants.EXP_MINUS_1) return Double.NaN;
		double q = x + MathConstants.EXP_MINUS_1;
		if (q < 1.0e-03) return seriesEval(Math.sqrt(q));
		double w;
		if (x < 1) {
			final double p = Math.sqrt(2.0 * MathConstants.E * q);
			w = -1.0 + p * (1.0 + p * (-1.0 / 3.0 + p * 11.0 / 72.0));
		}
		else {
			w = Math.log(x);
			if (x > 3.0) w -= Math.log(w);
		}
		return halleyIteration(x, w);
	}
	private static double lambertW1(double x) {
		if (x >= -BinaryRelations.DEFAULT_COMPARISON_EPSILON) return Double.NaN;
		if (x < -MathConstants.EXP_MINUS_1) return Double.NaN;
		if (Math.abs(x + MathConstants.EXP_MINUS_1) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return -1;
		double M1 = 0.3361;
		double M2 = -0.0042;
		double M3 = -0.0201;
		double s = -1 - Math.log(-x);
		return -1.0 - s - (2.0/M1) * ( 1.0 - 1.0 / ( 1.0 + ( (M1 * Math.sqrt(s/2.0)) / (1.0 + M2 * s * Math.exp(M3 * Math.sqrt(s)) ) ) ) );
	}
	public static double lambertW(double x, double branch) {
		if (Double.isNaN(x)) return Double.NaN;
		if (Double.isNaN(branch)) return Double.NaN;
		if (Math.abs(branch) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return lambertW0(x);
		if (Math.abs(branch + 1) <= BinaryRelations.DEFAULT_COMPARISON_EPSILON) return lambertW1(x);
		return Double.NaN;
	}
}