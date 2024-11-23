package ua.edu.ucu.apps;

import java.sql.*;
import java.util.Optional;

public class CachedDocument extends AbstractDecorator {

    private static final String DB_URL = "jdbc:sqlite:cache.db";
    private static final String CREATE_TABLE_QUERY = 
        "CREATE TABLE IF NOT EXISTS Cache (Path TEXT PRIMARY KEY, Content TEXT)";
    private static final String SELECT_CONTENT_QUERY = 
        "SELECT Content FROM Cache WHERE Path = ?";
    private static final String REPLACE_CONTENT_QUERY = 
        "REPLACE INTO Cache (Path, Content) VALUES (?, ?)";
    
    private final String gcsPath;

    public CachedDocument(Document document, String gcsPath) {
        super(document);
        this.gcsPath = gcsPath;
        initializeCache();
    }

    private void initializeCache() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize cache database", e);
        }
    }

    private Optional<String> getCachedContent() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(SELECT_CONTENT_QUERY)) {
            pstmt.setString(1, gcsPath);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("Content"));
            }
        } catch (SQLException e) {
            System.err.println("Error while fetching cached content: " + e.getMessage());
        }
        return Optional.empty();
    }

    private void cacheContent(String content) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(REPLACE_CONTENT_QUERY)) {
            pstmt.setString(1, gcsPath);
            pstmt.setString(2, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error while caching content: " + e.getMessage());
        }
    }

    @Override
    public String parse() {
        return getCachedContent().orElseGet(() -> {
            String result = super.parse();
            cacheContent(result);
            return result;
        });
    }
}
