package com.studio.util;

import com.studio.exception.BusinessException;
import com.studio.model.RecordingOrder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Экспорт заказов на студийную запись в файл Excel (.xlsx).
 * Использует Apache POI (XSSFWorkbook).
 */
public final class ExcelExporter {

    private ExcelExporter() {
        // утилитный класс
    }

    /**
     * Экспортирует список заказов в .xlsx-файл.
     *
     * @param orders   список заказов
     * @param fileName путь к создаваемому файлу (например, "orders.xlsx")
     */
    public static void exportOrders(List<RecordingOrder> orders, String fileName) {
        if (orders == null) {
            throw new BusinessException("Список заказов для экспорта не задан");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("Имя файла для экспорта не задано");
        }
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName = fileName + ".xlsx";
        }

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Заказы");

            // Стиль заголовка
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Заголовки
            String[] headers = {
                    "ID", "Название", "Описание", "Статус",
                    "Тип записи", "Приоритет", "ID клиента",
                    "Дата записи", "Часы", "Стоимость (руб.)"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Данные
            int rowIdx = 1;
            for (RecordingOrder r : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(nullSafe(r.getTitle()));
                row.createCell(2).setCellValue(nullSafe(r.getDescription()));
                row.createCell(3).setCellValue(r.getStatus() == null ? "" : r.getStatus().name());
                row.createCell(4).setCellValue(r.getType() == null ? "" : r.getType().name());
                row.createCell(5).setCellValue(r.getPriority() == null ? "" : r.getPriority().name());
                row.createCell(6).setCellValue(r.getClientId());
                row.createCell(7).setCellValue(
                        r.getRecordingDate() == null ? "" : r.getRecordingDate().toString());
                row.createCell(8).setCellValue(r.getHours());
                row.createCell(9).setCellValue(r.getPrice());
            }

            // Автоширина колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Запись в файл
            Path path = Path.of(fileName);
            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                wb.write(out);
            }

            System.out.println("Excel-файл успешно сохранён: "
                    + path.toAbsolutePath());
            System.out.println("Строк выгружено: " + orders.size());
        } catch (IOException e) {
            throw new BusinessException("Ошибка экспорта в Excel: " + e.getMessage());
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}