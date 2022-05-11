package dev.l3g7.griefer_utils.misc.mxparser;

import dev.l3g7.griefer_utils.misc.mxparser.mathcollection.PrimesCache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class mXparser {
	static final int NOT_FOUND = -1;
	static final int FOUND = 0;
	public volatile static PrimesCache primesCache;
	static volatile boolean ulpRounding = false;
	static volatile boolean canonicalRounding = true;
	static volatile boolean almostIntRounding = true;
	static final int DEFAULT_MAX_RECURSION_CALLS = 200;
	static volatile int MAX_RECURSION_CALLS = DEFAULT_MAX_RECURSION_CALLS;
	static volatile List<String> tokensToRemove = new ArrayList<>();
	static volatile List<TokenModification> tokensToModify = new ArrayList<>();
	static volatile boolean degreesMode = false;
	static volatile int optionsChangesetNumber = 0;

	public static double[] arrayList2double(List<Double> numbers) {
		if (numbers == null)
			return null;
		int size = numbers.size();
		double[] newNumbers = new double[size];
		for (int i = 0; i < size; i++)
			newNumbers[i] = numbers.get(i);
		return newNumbers;
	}

	public static boolean checkIfCanonicalRounding() {
		return canonicalRounding;
	}

	public static boolean checkIfDegreesMode() {
		return degreesMode;
	}

	public static boolean regexMatch(String str, String pattern) {
		return Pattern.matches(pattern, str);
	}

	public static boolean isCurrentCalculationCancelled() {
		return false;
	}

	public static String NAMEv10 = "1.0";
	public static String NAMEv23 = "2.3";
	public static String NAMEv24 = "2.4";
	public static String NAMEv30 = "3.0";
	public static String NAMEv40 = "4.0";
	public static String NAMEv41 = "4.1";
	public static String NAMEv42 = "4.2";
}
