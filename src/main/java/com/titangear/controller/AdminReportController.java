package com.titangear.controller;

import com.titangear.config.RequirePermission;

import com.titangear.model.User;
import com.titangear.service.ExcelExportService;
import com.titangear.service.ReportService;
import com.titangear.service.UserService;
import com.titangear.service.WarehousePermissionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminReportController {

    @Autowired private ReportService reportService;
    @Autowired private ExcelExportService excelExportService;

    @RequirePermission(WarehousePermissionService.CAN_VIEW_REVENUE)
    @GetMapping("/report")
    public String report(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer chartYear,
            Authentication auth,
            Model model) {

        var productSales = (month != null || year != null)
                ? reportService.getProductsSalesByMonthYear(month, year)
                : reportService.getAllProductsSales();

        int selectedChartYear = (chartYear != null) ? chartYear : java.time.LocalDate.now().getYear();

        model.addAttribute("revenueByMonthForYear", reportService.getRevenueByMonthForYear(selectedChartYear));
        model.addAttribute("chartYear",        selectedChartYear);
        model.addAttribute("profitByMonth",    reportService.getProfitByMonth());
        model.addAttribute("profitByVariant",  reportService.getProfitByVariant());
        model.addAttribute("revenueByCategory",reportService.getRevenueByCategory());
        model.addAttribute("topProducts",      reportService.getTopProducts());
        model.addAttribute("summary",          reportService.getSummary());
        model.addAttribute("allProductsSales", productSales);
        model.addAttribute("selectedMonth",    month);
        model.addAttribute("selectedYear",     year);
        model.addAttribute("years",            reportService.getYearOptions());
        return "admin/revenue-report";
    }

    @GetMapping("/report/export")
    public void exportExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            HttpServletResponse response) throws Exception {

        String fileName = "BaoCaoDoanhThu";
        if (month != null) fileName += "_T" + month;
        if (year  != null) fileName += "_" + year;
        fileName += ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        byte[] data = excelExportService.exportRevenueReport(month, year);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }
}