package ru.r_mavlyutov.JStrimix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.r_mavlyutov.JStrimix.dao.ReportRepository;
import ru.r_mavlyutov.JStrimix.entity.Report;
import ru.r_mavlyutov.JStrimix.entity.ReportStatus;
import ru.r_mavlyutov.JStrimix.service.ReportService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    /**
     * Создает отчет и запускает процесс его формирования
     * Возвращает id отчета и НЕ дожидается окончания формирования
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport() {
        Long reportId = reportService.createReport();
        
        // Запускаем асинхронное формирование отчета
        reportService.generateReportAsync(reportId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("reportId", reportId);
        response.put("message", "Отчет создан и формирование запущено");
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Получает содержимое отчета по id
     * Если отчет еще не сформирован (статус CREATED) или завершился с ошибкой (статус ERROR),
     * возвращает соответствующую информацию
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
        
        Map<String, Object> response = new HashMap<>();
        response.put("reportId", report.getId());
        response.put("status", report.getStatus().name());
        
        if (report.getStatus() == ReportStatus.CREATED) {
            response.put("message", "Отчет еще формируется. Попробуйте позже.");
            return ResponseEntity.status(HttpStatus.PROCESSING).body(response);
        } else if (report.getStatus() == ReportStatus.ERROR) {
            response.put("message", "Ошибка при формировании отчета");
            response.put("content", report.getContent());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (report.getStatus() == ReportStatus.COMPLETED) {
            response.put("content", report.getContent());
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

