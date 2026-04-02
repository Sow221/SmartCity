
package com.smartcity.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import com.smartcity.utils.DatabaseConnection;

public abstract class BaseService {
    
    protected Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected <T> T withConnection(Supplier<T> operation) {
        try (Connection conn = getConn()) {
            if (conn == null) {
                return null;
            }
            return operation.get();
        } catch (SQLException e) {
            return null;
        }
    }

    protected int executeUpdate(String query, Object... params) {
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    protected <T> List<T> executeQuery(String query, Function<ResultSet, T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            // log
        }
        return results;
    }
}

