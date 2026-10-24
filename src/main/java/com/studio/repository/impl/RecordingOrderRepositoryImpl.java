package com.studio.repository.impl;

import com.studio.exception.DatabaseException;
import com.studio.model.*;
import com.studio.repository.RecordingOrderRepository;
import com.studio.config.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordingOrderRepositoryImpl implements RecordingOrderRepository {

    private static final String INSERT =
            "INSERT INTO recording_orders " +
            "(title, description, status, type, priority, client_id, " +
            " recording_date, hours, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    /**
     * Базовый SELECT: заказ + имя и телефон клиента (JOIN по внешнему ключу client_id).
     */
    private static final String BASE_SELECT =
            "SELECT o.*, c.full_name AS client_name, c.phone AS client_phone " +
            "FROM recording_orders o JOIN clients c ON c.id = o.client_id ";
    private static final String SELECT_BY_ID =
            BASE_SELECT + "WHERE o.id = ?";
    private static final String SELECT_ALL =
            BASE_SELECT + "ORDER BY o.id";
    private static final String UPDATE =
            "UPDATE recording_orders SET title=?, description=?, status=?, type=?, " +
            "priority=?, client_id=?, recording_date=?, hours=?, price=?, " +
            "updated_at=CURRENT_TIMESTAMP WHERE id=?";
    private static final String DELETE =
            "DELETE FROM recording_orders WHERE id=?";
    private static final String SELECT_BY_STATUS =
            BASE_SELECT + "WHERE o.status = ? ORDER BY o.id";
    private static final String SELECT_BY_TYPE =
            BASE_SELECT + "WHERE o.type = ? ORDER BY o.id";
    private static final String SELECT_BY_PRIORITY =
            BASE_SELECT + "WHERE o.priority = ? ORDER BY o.id";
    private static final String SELECT_BY_CLIENT =
            BASE_SELECT + "WHERE o.client_id = ? ORDER BY o.id";

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
            throw new DatabaseException("Не удалось загрузить заказ", e);
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
            throw new DatabaseException("Не удалось удалить заказ", e);
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
            throw new DatabaseException("Не удалось загрузить список заказов", e);
        }
    }

    private RecordingOrder mapRow(ResultSet rs) throws SQLException {
        RecordingOrder order = new RecordingOrder(
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
        order.setClientName(rs.getString("client_name"));
        order.setClientPhone(rs.getString("client_phone"));
        return order;
    }
}
