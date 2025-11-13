package ru.r_mavlyutov.JStrimix.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.r_mavlyutov.JStrimix.dao.ReportRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Report;
import ru.r_mavlyutov.JStrimix.entity.ReportStatus;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public ReportServiceImpl(ReportRepository reportRepository,
                            UserRepository userRepository,
                            VideoRepository videoRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    @Transactional
    public Long createReport() {
        Report report = new Report();
        report.setStatus(ReportStatus.CREATED);
        report.setContent("");
        Report saved = reportRepository.save(report);
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public String getReportContent(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        return report.getContent();
    }

    @Override
    @Async
    @Transactional
    public void generateReportAsync(Long reportId) {
        long totalStartTime = System.currentTimeMillis();

        try {
            // Запоминаем время начала для подсчета количества пользователей
            long userCountStartTime = System.currentTimeMillis();

            // Вычисление количества пользователей в отдельном потоке
            CompletableFuture<Long> userCountFuture = CompletableFuture.supplyAsync(() -> {
                return userRepository.count();
            });

            // Запоминаем время начала для получения списка видео
            long videoListStartTime = System.currentTimeMillis();

            // Получение списка видео во втором потоке
            CompletableFuture<List<Video>> videoListFuture = CompletableFuture.supplyAsync(() -> {
                // Используем метод с @EntityGraph для загрузки связанных сущностей
                return videoRepository.findAllWithAuthorAndCategory();
            });

            // Ждем завершения обоих потоков
            Long userCount = userCountFuture.join();
            long userCountElapsed = System.currentTimeMillis() - userCountStartTime;

            List<Video> videos = videoListFuture.join();
            long videoListElapsed = System.currentTimeMillis() - videoListStartTime;

            long totalElapsed = System.currentTimeMillis() - totalStartTime;

            // Формируем HTML отчет
            String htmlContent = generateHtmlReport(userCount, userCountElapsed, videos, videoListElapsed, totalElapsed);

            // Обновляем отчет
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
            report.setContent(htmlContent);
            report.setStatus(ReportStatus.COMPLETED);
            reportRepository.save(report);

        } catch (Exception e) {
            // Обновляем статус на ERROR при ошибке
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
            report.setStatus(ReportStatus.ERROR);
            report.setContent("Ошибка при формировании отчета: " + e.getMessage());
            reportRepository.save(report);
        }
    }

    private String generateHtmlReport(Long userCount, long userCountElapsed,
                                     List<Video> videos, long videoListElapsed,
                                     long totalElapsed) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Отчет приложения JStrimix</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #666; margin-top: 30px; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("        th, td { padding: 12px; text-align: left; border: 1px solid #ddd; }\n");
        html.append("        th { background-color: #4CAF50; color: white; }\n");
        html.append("        tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("        .statistics { background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
        html.append("        .statistics p { margin: 5px 0; }\n");
        html.append("        .time-info { color: #666; font-size: 0.9em; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>Отчет приложения JStrimix</h1>\n");

        // Статистика
        html.append("        <div class=\"statistics\">\n");
        html.append("            <h2>Статистика</h2>\n");
        html.append("            <p><strong>Количество зарегистрированных пользователей:</strong> ").append(userCount).append("</p>\n");
        html.append("            <p class=\"time-info\">Время вычисления: ").append(userCountElapsed).append(" мс</p>\n");
        html.append("            <p><strong>Количество видео:</strong> ").append(videos.size()).append("</p>\n");
        html.append("            <p class=\"time-info\">Время получения списка: ").append(videoListElapsed).append(" мс</p>\n");
        html.append("            <p><strong>Общее время формирования отчета:</strong> ").append(totalElapsed).append(" мс</p>\n");
        html.append("        </div>\n");

        // Список видео
        html.append("        <h2>Список видео</h2>\n");
        html.append("        <table>\n");
        html.append("            <thead>\n");
        html.append("                <tr>\n");
        html.append("                    <th>ID</th>\n");
        html.append("                    <th>Название</th>\n");
        html.append("                    <th>Автор</th>\n");
        html.append("                    <th>Категория</th>\n");
        html.append("                    <th>Дата создания</th>\n");
        html.append("                </tr>\n");
        html.append("            </thead>\n");
        html.append("            <tbody>\n");

        if (videos.isEmpty()) {
            html.append("                <tr><td colspan=\"5\" style=\"text-align: center;\">Видео не найдены</td></tr>\n");
        } else {
            for (Video video : videos) {
                html.append("                <tr>\n");
                html.append("                    <td>").append(video.getId()).append("</td>\n");
                html.append("                    <td>").append(escapeHtml(video.getTitle())).append("</td>\n");
                html.append("                    <td>").append(video.getAuthor() != null ? escapeHtml(video.getAuthor().getUsername()) : "N/A").append("</td>\n");
                html.append("                    <td>").append(video.getCategory() != null ? escapeHtml(video.getCategory().getName()) : "Без категории").append("</td>\n");
                html.append("                    <td>").append(video.getCreatedAt() != null ? video.getCreatedAt().toString() : "N/A").append("</td>\n");
                html.append("                </tr>\n");
            }
        }

        html.append("            </tbody>\n");
        html.append("        </table>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}

