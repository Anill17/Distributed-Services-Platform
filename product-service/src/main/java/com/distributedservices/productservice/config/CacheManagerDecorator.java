package com.distributedservices.productservice.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Wraps a CacheManager so that cache get/put errors (e.g. SerializationException, Redis down)
 * are caught and treated as cache miss / no-op, preventing 500s.
 */
@Slf4j
@RequiredArgsConstructor
public class CacheManagerDecorator implements CacheManager {

    private final CacheManager delegate;
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, n -> new SafeCache(delegate.getCache(n), n));
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames() != null ? delegate.getCacheNames() : Collections.emptyList();
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class SafeCache implements Cache {
        private final Cache delegate;
        private final String name;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            try {
                return delegate.get(key);
            } catch (Exception e) {
                log.warn("Cache get error (treating as miss), cache={}, key={}: {}", name, key, e.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            try {
                return delegate.get(key, type);
            } catch (Exception e) {
                log.warn("Cache get error (treating as miss), cache={}, key={}: {}", name, key, e.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            try {
                return delegate.get(key, valueLoader);
            } catch (Exception e) {
                log.warn("Cache get error (treating as miss), cache={}, key={}: {}", name, key, e.getMessage());
                try {
                    return valueLoader.call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        public void put(Object key, Object value) {
            try {
                delegate.put(key, value);
            } catch (Exception e) {
                log.warn("Cache put error, cache={}, key={}: {}", name, key, e.getMessage());
            }
        }

        @Override
        public void evict(Object key) {
            try {
                delegate.evict(key);
            } catch (Exception e) {
                log.warn("Cache evict error, cache={}, key={}: {}", name, key, e.getMessage());
            }
        }

        @Override
        public void clear() {
            try {
                delegate.clear();
            } catch (Exception e) {
                log.warn("Cache clear error, cache={}: {}", name, e.getMessage());
            }
        }
    }
}
