package dev.l3g7.griefer_utils.misc.mxparser.parsertokens;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;
public final class FunctionVariadic {
	public static final int TYPE_ID 					= 7;
	public static final String TYPE_DESC				= "Variadic Function";
	public static final int IFF_ID 						= 1;
	public static final int MIN_ID 						= 2;
	public static final int MAX_ID 						= 3;
	public static final int CONT_FRAC_ID				= 4;
	public static final int CONT_POL_ID					= 5;
	public static final int GCD_ID						= 6;
	public static final int LCM_ID						= 7;
	public static final int SUM_ID						= 8;
	public static final int PROD_ID						= 9;
	public static final int AVG_ID						= 10;
	public static final int VAR_ID						= 11;
	public static final int STD_ID						= 12;
	public static final int RND_LIST_ID					= 13;
	public static final int COALESCE_ID					= 14;
	public static final int OR_ID						= 15;
	public static final int AND_ID						= 16;
	public static final int XOR_ID						= 17;
	public static final int ARGMIN_ID					= 18;
	public static final int ARGMAX_ID					= 19;
	public static final int MEDIAN_ID					= 20;
	public static final int MODE_ID						= 21;
	public static final int BASE_ID						= 22;
	public static final int NDIST_ID					= 23;
	public static final String IFF_STR 					= "iff";
	public static final String MIN_STR					= "min";
	public static final String MAX_STR					= "max";
	public static final String CONT_FRAC_STR			= "ConFrac";
	public static final String CONT_POL_STR				= "ConPol";
	public static final String GCD_STR					= "gcd";
	public static final String LCM_STR					= "lcm";
	public static final String SUM_STR					= "add";
	public static final String PROD_STR					= "multi";
	public static final String AVG_STR					= "mean";
	public static final String VAR_STR					= "var";
	public static final String STD_STR					= "std";
	public static final String RND_LIST_STR				= "rList";
	public static final String COALESCE_STR				= "coalesce";
	public static final String OR_STR					= "or";
	public static final String AND_STR					= "and";
	public static final String XOR_STR					= "xor";
	public static final String ARGMIN_STR				= "argmin";
	public static final String ARGMAX_STR				= "argmax";
	public static final String MEDIAN_STR				= "med";
	public static final String MODE_STR					= "mode";
	public static final String BASE_STR					= "base";
	public static final String NDIST_STR				= "ndist";
	public static final String IFF_SYN 					= "iff( cond-1, expr-1; ... ; cond-n, expr-n )";
	public static final String MIN_SYN					= "min(a1, ..., an)";
	public static final String MAX_SYN					= "max(a1, ..., an)";
	public static final String CONT_FRAC_SYN			= "ConFrac(a1, ..., an)";
	public static final String CONT_POL_SYN				= "ConPol(a1, ..., an)";
	public static final String GCD_SYN					= "gcd(a1, ..., an)";
	public static final String LCM_SYN					= "lcm(a1, ..., an)";
	public static final String SUM_SYN					= "add(a1, ..., an)";
	public static final String PROD_SYN					= "multi(a1, ..., an)";
	public static final String AVG_SYN					= "mean(a1, ..., an)";
	public static final String VAR_SYN					= "var(a1, ..., an)";
	public static final String STD_SYN					= "std(a1, ..., an)";
	public static final String RND_LIST_SYN				= "rList(a1, ..., an)";
	public static final String COALESCE_SYN				= "coalesce(a1, ..., an)";
	public static final String OR_SYN					= "or(a1, ..., an)";
	public static final String AND_SYN					= "and(a1, ..., an)";
	public static final String XOR_SYN					= "xor(a1, ..., an)";
	public static final String ARGMIN_SYN				= "argmin(a1, ..., an)";
	public static final String ARGMAX_SYN				= "argmax(a1, ..., an)";
	public static final String MEDIAN_SYN				= "med(a1, ..., an)";
	public static final String MODE_SYN					= "mode(a1, ..., an)";
	public static final String BASE_SYN					= "base(b, d1, ..., dn)";
	public static final String NDIST_SYN				= "ndist(v1, ..., vn)";
	public static final String IFF_DESC 				= "If function";
	public static final String MIN_DESC 				= "Minimum function";
	public static final String MAX_DESC 				= "Maximum function";
	public static final String CONT_FRAC_DESC			= "Continued fraction";
	public static final String CONT_POL_DESC			= "Continued polynomial";
	public static final String GCD_DESC					= "Greatest common divisor";
	public static final String LCM_DESC					= "Least common multiple";
	public static final String SUM_DESC					= "Summation operator";
	public static final String PROD_DESC				= "Multiplication";
	public static final String AVG_DESC					= "Mean / average value";
	public static final String VAR_DESC					= "Bias-corrected sample variance";
	public static final String STD_DESC					= "Bias-corrected sample standard deviation";
	public static final String RND_LIST_DESC			= "Random number from given list of numbers";
	public static final String COALESCE_DESC			= "Returns the first non-NaN value";
	public static final String OR_DESC					= "Logical disjunction (OR) - variadic";
	public static final String AND_DESC					= "Logical conjunction (AND) - variadic";
	public static final String XOR_DESC					= "Exclusive or (XOR) - variadic";
	public static final String ARGMIN_DESC				= "Arguments / indices of the minima";
	public static final String ARGMAX_DESC				= "Arguments / indices of the maxima";
	public static final String MEDIAN_DESC				= "The sample median";
	public static final String MODE_DESC				= "Mode - the value that appears most often";
	public static final String BASE_DESC				= "Returns number in given numeral system base represented by list of digits";
	public static final String NDIST_DESC				= "Number of distinct values";
	public static final String IFF_SINCE 				= mXparser.NAMEv10;
	public static final String MIN_SINCE 				= mXparser.NAMEv10;
	public static final String MAX_SINCE 				= mXparser.NAMEv10;
	public static final String CONT_FRAC_SINCE			= mXparser.NAMEv10;
	public static final String CONT_POL_SINCE			= mXparser.NAMEv10;
	public static final String GCD_SINCE				= mXparser.NAMEv10;
	public static final String LCM_SINCE				= mXparser.NAMEv10;
	public static final String SUM_SINCE				= mXparser.NAMEv24;
	public static final String PROD_SINCE				= mXparser.NAMEv24;
	public static final String AVG_SINCE				= mXparser.NAMEv24;
	public static final String VAR_SINCE				= mXparser.NAMEv24;
	public static final String STD_SINCE				= mXparser.NAMEv24;
	public static final String RND_LIST_SINCE			= mXparser.NAMEv30;
	public static final String COALESCE_SINCE			= mXparser.NAMEv41;
	public static final String OR_SINCE					= mXparser.NAMEv41;
	public static final String AND_SINCE				= mXparser.NAMEv41;
	public static final String XOR_SINCE				= mXparser.NAMEv41;
	public static final String ARGMIN_SINCE				= mXparser.NAMEv41;
	public static final String ARGMAX_SINCE				= mXparser.NAMEv41;
	public static final String MEDIAN_SINCE				= mXparser.NAMEv41;
	public static final String MODE_SINCE				= mXparser.NAMEv41;
	public static final String BASE_SINCE				= mXparser.NAMEv41;
	public static final String NDIST_SINCE				= mXparser.NAMEv41;
}

