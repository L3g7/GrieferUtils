package dev.l3g7.griefer_utils.misc.mxparser.parsertokens;

public class Token {
	public static final int NOT_MATCHED = KeyWord.NO_DEFINITION;
	public String tokenStr;
	public String keyWord;
	public int tokenId;
	public int tokenTypeId;
	public int tokenLevel;
	public double tokenValue;
	public String looksLike;
	public Token() {
		tokenStr = "";
		keyWord = "";
		tokenId = NOT_MATCHED;
		tokenTypeId = NOT_MATCHED;
		tokenLevel = -1;
		tokenValue = Double.NaN;
		looksLike = "";
	}
	public Token clone() {
		Token token = new Token();
		token.keyWord = keyWord;
		token.tokenStr = tokenStr;
		token.tokenId = tokenId;
		token.tokenLevel = tokenLevel;
		token.tokenTypeId = tokenTypeId;
		token.tokenValue = tokenValue;
		token.looksLike = looksLike;
		return token;
	}
}
