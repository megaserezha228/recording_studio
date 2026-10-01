package com.studio.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class RecordingOrder {
    private int id;
    private String title;
    private String description;
    private OrderStatus status;
    private RecordingType type;
    private Priority priority;
    private int clientId;
    private LocalDate recordingDate;
    private int hours;
    private int price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecordingOrder() { }

    public RecordingOrder(int id, String title, String description,
                          OrderStatus status, RecordingType type, Priority priority,
                          int clientId, LocalDate recordingDate, int hours, int price,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.priority = priority;
        this.clientId = clientId;
        this.recordingDate = recordingDate;
        this.hours = hours;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Конструктор для создания нового заказа (без id и timestamps). */
    public RecordingOrder(String title, String description,
                          OrderStatus status, RecordingType type, Priority priority,
                          int clientId, LocalDate recordingDate, int hours, int price) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.priority = priority;
        this.clientId = clientId;
        this.recordingDate = recordingDate;
        this.hours = hours;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public RecordingType getType() { return type; }
    public void setType(RecordingType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public LocalDate getRecordingDate() { return recordingDate; }
    public void setRecordingDate(LocalDate recordingDate) { this.recordingDate = recordingDate; }

    public int getHours() { return hours; }
    public void setHours(int hours) { this.hours = hours; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format(
                "RecordingOrder{id=%d, title='%s', status=%s, type=%s, priority=%s, " +
                "clientId=%d, date=%s, hours=%d, price=%d}",
                id, title, status, type, priority, clientId, recordingDate, hours, price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordingOrder order)) return false;
        return id == order.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}