package ai.hual.labrador.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LoggerHashMap<K, V> extends HashMap<K, V> {
    private Logger logger = LoggerFactory.getLogger(LoggerHashMap.class);

    public LoggerHashMap() {
        super();
    }

    public LoggerHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }


    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    public V putWithLog(K key, V value) {
        logger.debug("Slot {} updated to {}", key, value);
        return super.put(key, value);
    }
}
