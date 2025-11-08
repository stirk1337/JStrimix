package ru.r_mavlyutov.JStrimix.service;

public interface ReportService {

    /**
     * Создает отчет в БД со статусом CREATED и возвращает его id
     */
    Long createReport();

    /**
     * Получает содержимое отчета по id
     */
    String getReportContent(Long reportId);

    /**
     * Асинхронно формирует отчет
     */
    void generateReportAsync(Long reportId);
}

