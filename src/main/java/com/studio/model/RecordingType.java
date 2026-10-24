package com.studio.model;

/**
 * Тип записи. Ставка за час используется в бизнес-расчёте стоимости.
 * name() хранится в БД, title — показывается пользователю.
 */
public enum RecordingType {
    SINGLE("Сингл", 3000),
    ALBUM("Альбом", 2500),
    JINGLE("Джингл", 4000),
    VOICEOVER("Озвучка", 2000),
    MIXING("Сведение", 3500);

    private final String title;
    private final int hourlyRate;

    RecordingType(String title, int hourlyRate) {
        this.title = title;
        this.hourlyRate = hourlyRate;
    }

    public String getTitle() {
        return title;
    }

    public int getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public String toString() {
        return title;
    }
}
