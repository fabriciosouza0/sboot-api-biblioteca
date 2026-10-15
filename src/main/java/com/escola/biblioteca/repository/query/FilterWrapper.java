package com.escola.biblioteca.repository.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record FilterWrapper(Map<String, Object> filters) {

    public FilterWrapper {
        filters = (filters != null) ? Collections.unmodifiableMap(new HashMap<>(filters)) : Collections.emptyMap();
    }

    public static FilterWrapper of(Map<String, Object> filters) {
        return new FilterWrapper(filters);
    }

    public static FilterWrapper empty() {
        return new FilterWrapper(Collections.emptyMap());
    }

    public Object get(String key) {
        return filters.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object val = filters.get(key);
        if (val == null) return Optional.empty();
        if (val instanceof String s && s.isBlank()) return Optional.empty();
        if (type.isInstance(val)) {
            return Optional.of((T) val);
        }
        return Optional.empty();
    }

    public Optional<String> getString(String key) {
        Object val = filters.get(key);
        if (val == null) return Optional.empty();
        String str = val.toString().trim();
        return str.isBlank() ? Optional.empty() : Optional.of(str);
    }

    public boolean has(String key) {
        Object val = filters.get(key);
        if (val == null) return false;
        if (val instanceof String s && (s.isBlank() || "ALL".equalsIgnoreCase(s.trim()))) return false;
        return true;
    }
}
