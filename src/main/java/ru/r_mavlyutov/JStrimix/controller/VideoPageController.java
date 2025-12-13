package ru.r_mavlyutov.JStrimix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VideoPageController {

    @GetMapping("/videos/list")
    public String listVideos() {
        // Перенаправляем на новый контроллер
        return "redirect:/videos";
    }
}
