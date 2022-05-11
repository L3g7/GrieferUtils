package dev.l3g7.griefer_utils.misc.mxparser.parsertokens;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;
public final class ParserSymbol {
	public static final String DIGIT							= "[0-9]";
	public static final String INTEGER							= DIGIT + "(" + DIGIT + ")*";
	public static final String DEC_FRACT						= "(" + INTEGER + ")?" + "\\." + INTEGER;
	public static final String DEC_FRACT_OR_INT					= "(" + DEC_FRACT + "|" + INTEGER + ")";
	public static final String DECIMAL_REG_EXP					= "[+-]?" + DEC_FRACT_OR_INT + "([eE][+-]?" + INTEGER + ")?";
	public static final String FRACTION							= "(" + INTEGER + "\\_)?" + INTEGER + "\\_" + INTEGER;
	public static final String nameOnlyTokenRegExp				= "([a-zA-Z_])+([a-zA-Z0-9_])*";
	public static final String unitOnlyTokenRegExp				= "\\[" + nameOnlyTokenRegExp + "\\]";
	public static final int TYPE_ID 						= 20;
	public static final String TYPE_DESC					= "Parser Symbol";
	public static final int LEFT_PARENTHESES_ID 			= 1;
	public static final int RIGHT_PARENTHESES_ID			= 2;
	public static final int COMMA_ID						= 3;
	public static final int BLANK_ID						= 4;
	public static final int NUMBER_ID						= 1;
	public static final int NUMBER_TYPE_ID					= 0;

	public static final String LEFT_PARENTHESES_STR 		= "(";
	public static final String RIGHT_PARENTHESES_STR		= ")";
	public static final String COMMA_STR					= ",";
	public static final String SEMI_STR						= ";";
	public static final String BLANK_STR					= " ";
	public static final String NUMBER_STR					= "_num_";
	public static final String LEFT_PARENTHESES_SYN 		= "( ... )";
	public static final String RIGHT_PARENTHESES_SYN		= "( ... )";
	public static final String COMMA_SYN					= "(a1, ... ,an)";
	public static final String SEMI_SYN						= "(a1; ... ;an)";
	public static final String BLANK_SYN					= " ";
	public static final String NUMBER_SYN					= "Integer (since v.1.0): 1, -2; Decimal (since v.1.0): 0.2, -0.3, 1.2; Leading zero (since v.4.1): 001, -002.1; Scientific notation (since v.4.2): 1.2e-10, 1.2e+10, 2.3e10; No leading zero (since v.4.2): .2, -.212; Fractions (since v.4.2): 1_2, 2_1_3, 14_3; Other systems (since v.4.1): b1.111, b2.1001, b3.12021, b16.af12, h.af1, b.1001, o.0127";
	public static final String LEFT_PARENTHESES_DESC 		= "Left parentheses";
	public static final String RIGHT_PARENTHESES_DESC		= "Right parentheses";
	public static final String COMMA_DESC					= "Comma (function parameters)";
	public static final String SEMI_DESC					= "Semicolon (function parameters)";
	public static final String BLANK_DESC					= "Blank (whitespace) character";
	public static final String NUMBER_REG_DESC				= "Regullar expression for decimal numbers";
	public static final String LEFT_PARENTHESES_SINCE 		= mXparser.NAMEv10;
	public static final String RIGHT_PARENTHESES_SINCE		= mXparser.NAMEv10;
	public static final String COMMA_SINCE					= mXparser.NAMEv10;
	public static final String BLANK_SINCE					= mXparser.NAMEv42;
	public static final String NUMBER_SINCE					= mXparser.NAMEv10;
}
