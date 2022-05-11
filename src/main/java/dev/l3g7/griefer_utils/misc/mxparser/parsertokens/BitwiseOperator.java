package dev.l3g7.griefer_utils.misc.mxparser.parsertokens;

import dev.l3g7.griefer_utils.misc.mxparser.mXparser;
public final class BitwiseOperator {
	public static final int TYPE_ID 				= 11;
	public static final String TYPE_DESC			= "Bitwise Operator";
	public static final int COMPL_ID				= 1;
	public static final int AND_ID					= 2;
	public static final int XOR_ID					= 3;
	public static final int OR_ID					= 4;
	public static final int LEFT_SHIFT_ID			= 5;
	public static final int RIGHT_SHIFT_ID			= 6;
	public static final String COMPL_STR			= "@~";
	public static final String AND_STR				= "@&";
	public static final String XOR_STR				= "@^";
	public static final String OR_STR				= "@|";
	public static final String LEFT_SHIFT_STR		= "@<<";
	public static final String RIGHT_SHIFT_STR		= "@>>";
	public static final String COMPL_SYN			= "@~a";
	public static final String AND_SYN				= "a @& b";
	public static final String XOR_SYN				= "a @^ b";
	public static final String OR_SYN				= "a @| b";
	public static final String LEFT_SHIFT_SYN		= "a @<< b";
	public static final String RIGHT_SHIFT_SYN		= "a @>> b";
	public static final String COMPL_DESC			= "Bitwise unary complement";
	public static final String AND_DESC				= "Bitwise AND";
	public static final String XOR_DESC				= "Bitwise exclusive OR";
	public static final String OR_DESC				= "Bitwise inclusive OR";
	public static final String LEFT_SHIFT_DESC		= "Signed left shift";
	public static final String RIGHT_SHIFT_DESC		= "Signed right shift";
	public static final String COMPL_SINCE			= mXparser.NAMEv40;
	public static final String AND_SINCE			= mXparser.NAMEv40;
	public static final String XOR_SINCE			= mXparser.NAMEv40;
	public static final String OR_SINCE				= mXparser.NAMEv40;
	public static final String LEFT_SHIFT_SINCE		= mXparser.NAMEv40;
	public static final String RIGHT_SHIFT_SINCE	= mXparser.NAMEv40;
}

