package dev.l3g7.griefer_utils.misc.mxparser;

import dev.l3g7.griefer_utils.misc.mxparser.parsertokens.KeyWord;

import java.util.Comparator;

class TokenStackElement {
	int tokenIndex;
	int tokenId;
	int tokenTypeId;
	int tokenLevel;
	boolean precedingFunction;
}

class KwStrComparator implements Comparator<KeyWord> {
	public int compare(KeyWord kw1, KeyWord kw2) {
		String s1 = kw1.wordString;
		String s2 = kw2.wordString;
		return s1.compareTo(s2);
	}
}

class DescKwLenComparator implements Comparator<KeyWord> {
	public int compare(KeyWord kw1, KeyWord kw2) {
		int l1 = kw1.wordString.length();
		int l2 = kw2.wordString.length();
		return l2 - l1;
	}
}

class TokenModification {
	String currentToken;
	String newToken;
	String newTokenDescription;
}