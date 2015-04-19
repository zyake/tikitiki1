package tikitiki.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private volatile static CacheManager instance = new CacheManager(Collections.<String, Object>emptyMap());

    private final Map<String, Object> cache;

    public static CacheManager getInstance() {
        return instance;
    }

    public static void replaceAtomically(Map<String, Object> cache) {
        // キャッシュの置き換えは常にアトミック。
        instance = new CacheManager(cache);
    }

    private CacheManager(Map<String, Object> cache) {
        this.cache = new HashMap<>(cache);
    }

    public <V> V get(String key) {
        return (V) cache.get(key);
    }
}
