package com.studio.repository.impl;

import com.studio.exception.DatabaseException;
import com.studio.model.Client;
import com.studio.model.ClientRole;
import com.studio.repository.ClientRepository;
import com.studio.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientRepositoryImpl implements ClientRepository {

    private static final String INSERT =
            "INSERT INTO clients (full_name, email, phone, role) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID =
            "SELECT * FROM clients WHERE id = ?";
    private static final String SELECT_BY_EMAIL =
            "SELECT * FROM clients WHERE email = ?";
    private static final String SELECT_ALL =
            "SELECT * FROM clients ORDER BY id";
    private static final String UPDATE =
            "UPDATE clients SET full_name=?, email=?, phone=?, role=? WHERE id=?";
    private static final String DELETE =
            "DELETE FROM clients WHERE id=?";
    private static final String EXISTS =
            "SELECT 1 FROM clients WHERE id=?";

    @Override
    public Client save(Client client) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getRole().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) client.setId(keys.getInt(1));
            }
            return client;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка сохранения клиента: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Client> findById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка поиска клиента", e);
        }
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка поиска клиента по email", e);
        }
    }

    @Override
    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка выборки клиентов", e);
        }
    }

    @Override
    public boolean update(Client client) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getRole().name());
            ps.setInt(5, client.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка обновления клиента", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка удаления клиента", e);
        }
    }

    @Override
    public boolean existsById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка проверки клиента", e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                ClientRole.valueOf(rs.getString("role")),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}