package com.studio.ui;

import com.studio.exception.BusinessException;
import com.studio.exception.DatabaseException;
import com.studio.exception.EntityNotFoundException;

public final class ExceptionHandler {

    private ExceptionHandler() { }

    public static void run(Runnable action) {
        try {
            action.run();
        } catch (EntityNotFoundException e) {
            System.out.println("[Не найдено] " + e.getMessage());
        } catch (BusinessException e) {
            System.out.println("[Бизнес-правило] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[Ошибка БД] " + e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                System.out.println("  Причина: " + cause.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("[Ошибка ввода] Ожидалось число.");
        } catch (Exception e) {
            System.out.println("[Неизвестная ошибка] " + e.getMessage());
        }
    }
}