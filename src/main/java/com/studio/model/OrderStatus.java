package com.studio.model;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) return false;
        return switch (this) {
            case CREATED     -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED   -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED   -> false;
            case CANCELLED   -> false;
        };
    }
}
