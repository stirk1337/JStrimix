package ru.r_mavlyutov.JStrimix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;
import ru.r_mavlyutov.JStrimix.service.*;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final VideoService videoService;
    private final SubscriptionService subscriptionService;

    @GetMapping
    public String myProfile(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Video> videos = videoService.findByAuthorId(user.getId());
        Long subscriberCount = subscriptionService.getSubscriberCount(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("videos", videos);
        model.addAttribute("subscriberCount", subscriberCount);
        model.addAttribute("isOwnProfile", true);
        return "profile/view";
    }

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable String username,
                             Authentication authentication,
                             Model model) {
        User profileUser = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<Video> videos = videoService.findByAuthorId(profileUser.getId());
        Long subscriberCount = subscriptionService.getSubscriberCount(profileUser.getId());

        model.addAttribute("user", profileUser);
        model.addAttribute("videos", videos);
        model.addAttribute("subscriberCount", subscriberCount);
        model.addAttribute("isOwnProfile", false);

        if (authentication != null) {
            User currentUser = userService.findByUsername(authentication.getName())
                    .orElse(null);
            if (currentUser != null) {
                model.addAttribute("isSubscribed", subscriptionService.isSubscribed(currentUser.getId(), profileUser.getId()));
                model.addAttribute("isOwnProfile", currentUser.getId().equals(profileUser.getId()));
            }
        }

        return "profile/view";
    }

    @PostMapping("/{username}/subscribe")
    public String subscribe(@PathVariable String username,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User channelUser = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        try {
            subscriptionService.subscribe(currentUser.getId(), channelUser.getId());
            redirectAttributes.addFlashAttribute("success", "Вы подписались на " + username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при подписке: " + e.getMessage());
        }
        return "redirect:/profile/" + username;
    }

    @PostMapping("/{username}/unsubscribe")
    public String unsubscribe(@PathVariable String username,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User channelUser = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        subscriptionService.unsubscribe(currentUser.getId(), channelUser.getId());
        redirectAttributes.addFlashAttribute("success", "Вы отписались от " + username);
        return "redirect:/profile/" + username;
    }
}

