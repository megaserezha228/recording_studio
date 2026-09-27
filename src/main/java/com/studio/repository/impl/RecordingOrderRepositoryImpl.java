package com.studio.repository.impl;

import com.studio.exception.DatabaseException;
import com.studio.model.*;
import com.studio.repository.RecordingOrderRepository;
import com.studio.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordingOrderRepositoryImpl implements RecordingOrderRepository {

    private static final String INSERT =
            "INSERT INTO recording_orders " +
            "(title, description, status, type, priority, client_id, " +
            " recording_date, hours, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID =
            "SELECT * FROM recording_orders WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT * FROM recording_orders ORDER BY id";
    private static final String UPDATE =
            "UPDATE recording_orders SET title=?, description=?, status=?, type=?, " +
            "priority=?, client_id=?, recording_date=?, hours=?, price=?, " +
            "updated_at=CURRENT_TIMESTAMP WHERE id=?";
    private static final String DELETE =
            "DELETE FROM recording_orders WHERE id=?";
    private static final String SELECT_BY_STATUS =
            "SELECT * FROM recording_orders WHERE status = ?";
    private static final String SELECT_BY_TYPE =
            "SELECT * FROM recording_orders WHERE type = ?";
    private static final String SELECT_BY_PRIORITY =
            "SELECT * FROM recording_orders WHERE priority = ?";
    private static final String SELECT_BY_CLIENT =
            "SELECT * FROM recording_orders WHERE client_id = ?";

    @Override
    public RecordingOrder save(RecordingOrder order) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getTitle());
            ps.setString(2, order.getDescription());
            ps.setString(3, order.getStatus().name());
            ps.setString(4, order.getType().name());
            ps.setString(5, order.getPriority().name());
            ps.setInt(6, order.getClientId());
            ps.setDate(7, Date.valueOf(order.getRecordingDate()));
            ps.setInt(8, order.getHours());
            ps.setInt(9, order.getPrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) order.setId(keys.getInt(1));
            }
            return order;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка сохранения заказа: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<RecordingOrder> findById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка поиска заказа", e);
        }
    }

    @Override
    public List<RecordingOrder> findAll() {
        return executeQuery(SELECT_ALL, null);
    }

    @Override
    public boolean update(RecordingOrder order) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, order.getTitle());
            ps.setString(2, order.getDescription());
            ps.setString(3, order.getStatus().name());
            ps.setString(4, order.getType().name());
            ps.setString(5, order.getPriority().name());
            ps.setInt(6, order.getClientId());
            ps.setDate(7, Date.valueOf(order.getRecordingDate()));
            ps.setInt(8, order.getHours());
            ps.setInt(9, order.getPrice());
            ps.setInt(10, order.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка обновления заказа", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка удаления заказа", e);
        }
    }

    @Override
    public List<RecordingOrder> findByStatus(OrderStatus status) {
        return executeQuery(SELECT_BY_STATUS, status.name());
    }

    @Override
    public List<RecordingOrder> findByType(RecordingType type) {
        return executeQuery(SELECT_BY_TYPE, type.name());
    }

    @Override
    public List<RecordingOrder> findByPriority(Priority priority) {
        return executeQuery(SELECT_BY_PRIORITY, priority.name());
    }

    @Override
    public List<RecordingOrder> findByClientId(int clientId) {
        List<RecordingOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CLIENT)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка выборки заказов клиента", e);
        }
    }

    private List<RecordingOrder> executeQuery(String sql, String param) {
        List<RecordingOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка выборки заказов", e);
        }
    }

    private RecordingOrder mapRow(ResultSet rs) throws SQLException {
        return new RecordingOrder(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                OrderStatus.valueOf(rs.getString("status")),
                RecordingType.valueOf(rs.getString("type")),
                Priority.valueOf(rs.getString("priority")),
                rs.getInt("client_id"),
                rs.getDate("recording_date").toLocalDate(),
                rs.getInt("hours"),
                rs.getInt("price"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}