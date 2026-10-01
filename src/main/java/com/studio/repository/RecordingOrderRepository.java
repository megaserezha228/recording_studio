package com.studio.repository;

import com.studio.model.OrderStatus;
import com.studio.model.Priority;
import com.studio.model.RecordingOrder;
import com.studio.model.RecordingType;

import java.util.List;
import java.util.Optional;

public interface RecordingOrderRepository {
    RecordingOrder save(RecordingOrder order);
    Optional<RecordingOrder> findById(int id);
    List<RecordingOrder> findAll();
    boolean update(RecordingOrder order);
    boolean deleteById(int id);
    List<RecordingOrder> findByStatus(OrderStatus status);
    List<RecordingOrder> findByType(RecordingType type);
    List<RecordingOrder> findByPriority(Priority priority);
    List<RecordingOrder> findByClientId(int clientId);
}