package com.xkball.auto_translate.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TranslationCacheSlice {
    
    private final Connection conn;
    public final String tableName;
    
    public TranslationCacheSlice(Connection conn, String tableName) {
        this.conn = conn;
        this.tableName = tableName;
    }
    
    public String get(String key){
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT value FROM " + tableName + " WHERE key = ?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("value") : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void put(String key, String value){
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + tableName + " (key, value) VALUES (?, ?) " +
                        "ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Map<String, String> toMap(){
        Map<String, String> result = new HashMap<>();
        String sql = "SELECT key, value FROM " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    public static class InMemory extends TranslationCacheSlice {
        
        private final Map<String, String> cache = new HashMap<>();
        
        public InMemory( String tableName) {
            super(null, tableName);
        }
        
        @Override
        public void put(String key, String value) {
            cache.put(key, value);
        }
        
        @Override
        public String get(String key) {
            return cache.get(key);
        }
        
        @Override
        public Map<String, String> toMap() {
            return Map.copyOf(cache);
        }
    }
}
