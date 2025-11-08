package ru.r_mavlyutov.JStrimix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VideoPageController {

    private final VideoRepository videoRepository;

    @GetMapping("/videos/list")
    public String listVideos(Model model) {
        List<Video> videos = videoRepository.findAll();
        model.addAttribute("videos", videos);
        return "videos-list"; // имя HTML файла (videos-list.html)
    }
}
