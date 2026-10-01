package com.studio.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Client {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private ClientRole role;
    private LocalDateTime createdAt;

    public Client() { }

    public Client(int id, String fullName, String email,
                  String phone, ClientRole role, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Client(String fullName, String email, String phone, ClientRole role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public ClientRole getRole() { return role; }
    public void setRole(ClientRole role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format(
                "Client{id=%d, fullName='%s', email='%s', phone='%s', role=%s}",
                id, fullName, email, phone, role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client client)) return false;
        return id == client.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}