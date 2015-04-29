package tikitiki.util;

public class CacheManager {

    private volatile static CacheManager instance = new CacheManager(new byte[0][0]);

    private final byte[][] cache;

    public static CacheManager getInstance() {
        return instance;
    }

    public static void replaceAtomically(byte[][] cache) {
        // キャッシュの置き換えは常にアトミック。
        instance = new CacheManager(cache);
    }

    private CacheManager(byte[][] cache) {
        this.cache = cache;
    }

    public byte[][] getCache() {
        return cache;
    }
}
