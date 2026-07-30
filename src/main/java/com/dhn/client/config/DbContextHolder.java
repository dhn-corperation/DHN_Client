package com.dhn.client.config;

public class DbContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setDbTarget(String dbType) {
        CONTEXT.set(dbType);
    }

    public static String getDbTarget() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}