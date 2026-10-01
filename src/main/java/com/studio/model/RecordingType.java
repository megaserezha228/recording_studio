package com.studio.model;

/**
 * Тип записи. Ставка за час используется в бизнес-расчёте стоимости.
 */
public enum RecordingType {
    SINGLE(3000),
    ALBUM(2500),
    JINGLE(4000),
    VOICEOVER(2000),
    MIXING(3500);

    private final int hourlyRate;

    RecordingType(int hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getHourlyRate() {
        return hourlyRate;
    }
}