package com.titangear.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExcelExportService {

    @Autowired
    private ReportService reportService;

    public byte[] exportRevenueReport(Integer month, Integer year) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ===== STYLE =====
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            // ===== SHEET 1: CHI TIẾT SẢN PHẨM ĐÃ BÁN =====
            Sheet sheet1 = workbook.createSheet("Chi tiết sản phẩm");
            sheet1.setColumnWidth(0, 4000);
            sheet1.setColumnWidth(1, 8000);
            sheet1.setColumnWidth(2, 5000);
            sheet1.setColumnWidth(3, 4000);
            sheet1.setColumnWidth(4, 5000);

            // Tiêu đề
            Row titleRow = sheet1.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            String title = "BÁO CÁO DOANH THU – TITANGEAR";
            if (month != null) title += " – Tháng " + month;
            if (year  != null) title += " Năm " + year;
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            sheet1.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Header
            Row headerRow = sheet1.createRow(2);
            String[] headers1 = {"STT", "Tên sản phẩm", "Danh mục", "Số lượng bán", "Doanh thu"};
            for (int i = 0; i < headers1.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers1[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            List<Map<String, Object>> products = (month != null || year != null)
                    ? reportService.getProductsSalesByMonthYear(month, year)
                    : reportService.getAllProductsSales();

            int rowNum = 3;
            long totalRevenue = 0;
            long totalQty = 0;
            for (Map<String, Object> p : products) {
                Row row = sheet1.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 3);
                row.createCell(1).setCellValue(str(p.get("ten_sp")));
                row.createCell(2).setCellValue(str(p.get("danh_muc")));
                long qty = toLong(p.get("so_luong_ban"));
                long rev = toLong(p.get("doanh_thu"));
                Cell qtyCell = row.createCell(3);
                qtyCell.setCellValue(qty);
                Cell revCell = row.createCell(4);
                revCell.setCellValue(rev);
                revCell.setCellStyle(currencyStyle);
                totalQty += qty;
                totalRevenue += rev;
            }

            // Tổng cộng
            Row totalRow = sheet1.createRow(rowNum + 1);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TỔNG CỘNG");
            totalLabel.setCellStyle(boldStyle);
            Cell totalQtyCell = totalRow.createCell(3);
            totalQtyCell.setCellValue(totalQty);
            totalQtyCell.setCellStyle(boldStyle);
            Cell totalRevCell = totalRow.createCell(4);
            totalRevCell.setCellValue(totalRevenue);
            totalRevCell.setCellStyle(currencyStyle);

            // ===== SHEET 2: DOANH THU THEO THÁNG =====
            Sheet sheet2 = workbook.createSheet("Doanh thu theo tháng");
            sheet2.setColumnWidth(0, 4000);
            sheet2.setColumnWidth(1, 3000);
            sheet2.setColumnWidth(2, 4000);
            sheet2.setColumnWidth(3, 5000);

            Row h2 = sheet2.createRow(0);
            String[] headers2 = {"Tháng", "Năm", "Số đơn", "Doanh thu"};
            for (int i = 0; i < headers2.length; i++) {
                Cell cell = h2.createCell(i);
                cell.setCellValue(headers2[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> byMonth = reportService.getRevenueByMonth();
            int r2 = 1;
            for (Map<String, Object> m : byMonth) {
                Row row = sheet2.createRow(r2++);
                row.createCell(0).setCellValue("Tháng " + m.get("thang"));
                row.createCell(1).setCellValue(toLong(m.get("nam")));
                row.createCell(2).setCellValue(toLong(m.get("so_don")));
                Cell c = row.createCell(3);
                c.setCellValue(toLong(m.get("doanh_thu")));
                c.setCellStyle(currencyStyle);
            }

            // ===== SHEET 3: TOP SẢN PHẨM BÁN CHẠY =====
            Sheet sheet3 = workbook.createSheet("Top bán chạy");
            sheet3.setColumnWidth(0, 3000);
            sheet3.setColumnWidth(1, 8000);
            sheet3.setColumnWidth(2, 4000);
            sheet3.setColumnWidth(3, 5000);

            Row h3 = sheet3.createRow(0);
            String[] headers3 = {"Hạng", "Tên sản phẩm", "Số lượng bán", "Doanh thu"};
            for (int i = 0; i < headers3.length; i++) {
                Cell cell = h3.createCell(i);
                cell.setCellValue(headers3[i]);
                cell.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> topProducts = reportService.getTopProducts();
            int r3 = 1;
            for (Map<String, Object> p : topProducts) {
                Row row = sheet3.createRow(r3);
                row.createCell(0).setCellValue("#" + r3++);
                row.createCell(1).setCellValue(str(p.get("ten_sp")));
                row.createCell(2).setCellValue(toLong(p.get("so_luong_ban")));
                Cell c = row.createCell(3);
                c.setCellValue(toLong(p.get("doanh_thu")));
                c.setCellStyle(currencyStyle);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private long toLong(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0; }
    }
}