package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;
public class PrimesCache {
	public static final boolean CACHING_FINISHED = true;
	public static final int IS_PRIME = 1;
	public static final int IS_NOT_PRIME = 0;
	public static final int NOT_IN_CACHE = -1;
	int maxNumInCache;
	boolean cacheStatus;
	boolean[] isPrime;
	public boolean getCacheStatus() {
		return cacheStatus;
	}
	public int primeTest(int n) {
		if (n <= 1) return IS_NOT_PRIME;
		if ( (n <= maxNumInCache) && (cacheStatus = CACHING_FINISHED) )
			if (isPrime[n])
				return IS_PRIME;
			else
				return IS_NOT_PRIME;
		else
			return NOT_IN_CACHE;
	}
}