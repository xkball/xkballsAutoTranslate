package com.xkball.auto_translate.data;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class XATDataBase {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final XATDataBase INSTANCE = createInstance();
    
    protected final Connection conn;
    
    private static XATDataBase createInstance(){
        try {
            var dir = FMLPaths.GAMEDIR.relative().resolve("data");
            //noinspection ResultOfMethodCallIgnored
            dir.toFile().mkdirs();
            var conn = DriverManager.getConnection("jdbc:sqlite:" + dir.resolve("xkball_s_auto_translate.db"));
            return new XATDataBase(conn);
        } catch (Exception e) {
            LOGGER.error("Couldn't open database", e);
            return new XATDataBase.InMemory();
        }
    }
    
    protected XATDataBase(Connection conn) {
        this.conn = conn;
        if(conn == null) return;
        try (Statement stmt = conn.createStatement()) {
            var createTokenCount = "CREATE TABLE IF NOT EXISTS extra_data (" +
                    "id    TEXT PRIMARY KEY," +
                    "value INTEGER NOT NULL)";
            var checkData = "SELECT COUNT(*) FROM extra_data";
            stmt.execute(createTokenCount);
            ResultSet rs = stmt.executeQuery(checkData);
            if (rs.next() && rs.getInt(1) == 0) {
                conn.createStatement().execute("INSERT INTO extra_data(id,value) VALUES('token_count',0)");
                conn.createStatement().execute("INSERT INTO extra_data(id,value) VALUES('inject_lang',0)");
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public TranslationCacheSlice createSlice(String name){
        try (Statement stmt = conn.createStatement()) {
            //noinspection SqlSourceToSinkFlow
            stmt.execute("CREATE TABLE IF NOT EXISTS "+ name + " (" +
                             "key   TEXT PRIMARY KEY NOT NULL, " +
                             "value TEXT             NOT NULL)");
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new TranslationCacheSlice(conn, name);
    }
    
    public synchronized void recordTokenCost(int count){
        String sql = "UPDATE extra_data SET value = value + ? WHERE id='token_count'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getTokenCost(){
        String sql = "SELECT value FROM extra_data WHERE id='token_count'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("value");
            }
        } catch (SQLException e) {
            LOGGER.error("SELECT failed: {}",sql, e);
        }
        return 0;
    }
    
    public void enableInjectLang(boolean enable){
        String sql = "UPDATE extra_data SET value = ? WHERE id='inject_lang'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setBoolean(1, enable);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean isEnableInjectLang(){
        String sql = "SELECT value FROM extra_data WHERE id='inject_lang'";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("value");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    
    public static class InMemory extends XATDataBase {
        
        private int cost = 0;
        
        protected InMemory() {
            super(null);
        }
        
        @Override
        public TranslationCacheSlice createSlice(String name) {
            return new TranslationCacheSlice.InMemory(name);
        }
        
        @Override
        public void recordTokenCost(int count) {
            cost += count;
        }
        
        @Override
        public int getTokenCost() {
            return cost;
        }
    }
}
