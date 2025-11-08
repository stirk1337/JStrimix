package ru.r_mavlyutov.JStrimix.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.service.VideoService;

import java.util.List;

@Component
public class CommandProcessor {

    private final VideoService videoService;

    @Autowired
    public CommandProcessor(VideoService videoService) {
        this.videoService = videoService;
    }


    public void processCommand(String input) {
        String[] cmd = input.trim().split(" ");

        switch (cmd[0].toLowerCase()) {
            case "upload" -> {
                if (cmd.length >= 5) {
                    Long id = Long.valueOf(cmd[1]);
                    String title = cmd[2];
                    String description = cmd[3];
                    int duration = Integer.parseInt(cmd[4]);

                    videoService.uploadVideo(id, title, description, duration);
                    System.out.println("Видео успешно загружено!");
                } else {
                    System.out.println("Использование: upload <id> <title> <description> <duration>");
                }
            }

            case "get" -> {
                if (cmd.length >= 2) {
                    Video video = videoService.getVideoById(Long.valueOf(cmd[1]));
                    if (video != null) {
                        System.out.println("Видео: " + video.getTitle());
                        System.out.println("Длительность: " + video.getDuration() + " сек");
                        System.out.println("Просмотры: " + video.getViews());
                        System.out.println("Описание: " + video.getDescription());
                    } else {
                        System.out.println("Видео не найдено!");
                    }
                }
            }

            case "list" -> {
                List<Video> videos = videoService.getAllVideos();
                if (videos.isEmpty()) {
                    System.out.println("Нет доступных видео");
                } else {
                    System.out.println("Список видео:");
                    for (Video video : videos) {
                        System.out.println("- " + video.getId() + ": " + video.getTitle());
                    }
                }
            }

            case "delete" -> {
                if (cmd.length >= 2) {
                    videoService.deleteVideo(Long.valueOf(cmd[1]));
                    System.out.println("Видео удалено!");
                }
            }

            case "watch" -> {
                if (cmd.length >= 2) {
                    videoService.incrementViews(Long.valueOf(cmd[1]));
                    System.out.println("Просмотр засчитан!");
                }
            }

            case "update" -> {
                if (cmd.length >= 4) {
                    videoService.updateVideoInfo(Long.valueOf(cmd[1]), cmd[2], cmd[3]);
                    System.out.println("Информация о видео обновлена!");
                }
            }

            case "help" -> {
                System.out.println("Доступные команды:");
                System.out.println("upload <id> <title> <description> <duration> - загрузить видео");
                System.out.println("get <id> - получить информацию о видео");
                System.out.println("list - список всех видео");
                System.out.println("delete <id> - удалить видео");
                System.out.println("watch <id> - увеличить счетчик просмотров");
                System.out.println("update <id> <title> <description> - обновить информацию");
                System.out.println("help - показать справку");
                System.out.println("exit - выход");
            }

            default -> System.out.println("Неизвестная команда. Введите 'help' для справки.");
        }
    }
}