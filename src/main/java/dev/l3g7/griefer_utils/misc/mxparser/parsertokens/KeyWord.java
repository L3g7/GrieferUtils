package dev.l3g7.griefer_utils.misc.mxparser.parsertokens;

public class KeyWord {
	public static final int NO_DEFINITION = ConstantValue.NaN;
	public String wordString;
	public int	wordId;
	public int wordTypeId;
	public String description;
	public String syntax;
	public String since;
	public KeyWord() {
		wordString = "";
		wordId = NO_DEFINITION;
		wordTypeId = NO_DEFINITION;
		description = "";
		syntax = "";
		since = "";
	}
	public KeyWord(String wordString, String description, int wordId, String syntax, String since, int wordTypeId) {
		this.wordString = wordString;
		this.wordId = wordId;
		this.wordTypeId = wordTypeId;
		this.description = description;
		this.syntax = syntax;
		this.since = since;
	}
}