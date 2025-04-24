package com.konnectnet.core.e2e.utils;

import java.util.HashMap;
import java.util.Map;

public class EntityContext {
    private static final Map<String, Object> store = new HashMap<>();

    // Save entity with a custom key
    public static <T> void add(String key, T value) {
        store.put(key, value);
    }

    // Get entity by key
    public static <T> T get(String key) {
        return (T) store.get(key);
    }


}
