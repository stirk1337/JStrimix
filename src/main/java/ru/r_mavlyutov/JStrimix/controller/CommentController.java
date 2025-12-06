package ru.r_mavlyutov.JStrimix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;
import ru.r_mavlyutov.JStrimix.service.CommentService;
import ru.r_mavlyutov.JStrimix.service.UserService;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @PostMapping("/create")
    public String createComment(@RequestParam("videoId") Long videoId,
                               @RequestParam("message") String message,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        commentService.createComment(user.getId(), videoId, message);
        redirectAttributes.addFlashAttribute("success", "Комментарий добавлен!");
        return "redirect:/videos/" + videoId;
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @RequestParam("videoId") Long videoId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            commentService.deleteComment(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Комментарий удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении комментария: " + e.getMessage());
        }
        return "redirect:/videos/" + videoId;
    }

    @PostMapping("/{id}/edit")
    public String updateComment(@PathVariable Long id,
                               @RequestParam("videoId") Long videoId,
                               @RequestParam("message") String message,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            commentService.updateComment(id, user.getId(), message);
            redirectAttributes.addFlashAttribute("success", "Комментарий обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении комментария: " + e.getMessage());
        }
        return "redirect:/videos/" + videoId;
    }
}

