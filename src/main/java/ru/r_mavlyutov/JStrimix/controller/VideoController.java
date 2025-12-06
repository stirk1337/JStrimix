package ru.r_mavlyutov.JStrimix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.r_mavlyutov.JStrimix.dao.CategoryRepository;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;
import ru.r_mavlyutov.JStrimix.service.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final CommentService commentService;
    private final VideoLikeService videoLikeService;
    private final VideoViewService videoViewService;
    private final SubscriptionService subscriptionService;
    private final FileStorageService fileStorageService;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @GetMapping
    public String listVideos(@RequestParam(required = false) String search,
                            @RequestParam(required = false) Long category,
                            Model model) {
        List<Video> videos;
        if (search != null && !search.trim().isEmpty()) {
            videos = videoService.searchVideos(search);
        } else if (category != null) {
            videos = videoService.findByCategory(category);
        } else {
            videos = videoService.findAll();
        }
        model.addAttribute("videos", videos);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        return "videos/list";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "videos/upload";
    }

    @PostMapping("/upload")
    public String uploadVideo(@RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("videoFile") MultipartFile videoFile,
                             @RequestParam(value = "previewFile", required = false) MultipartFile previewFile,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String videoPath = fileStorageService.storeFile(videoFile, "videos");
            String previewPath = null;
            if (previewFile != null && !previewFile.isEmpty()) {
                previewPath = fileStorageService.storeFile(previewFile, "previews");
            }

            Video video = videoService.createVideo(user.getId(), title, description, videoPath, previewPath, categoryId);
            redirectAttributes.addFlashAttribute("success", "Видео успешно загружено!");
            return "redirect:/videos/" + video.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке файла: " + e.getMessage());
            return "redirect:/videos/upload";
        }
    }

    @GetMapping("/{id}")
    public String viewVideo(@PathVariable Long id,
                           Authentication authentication,
                           Model model) {
        Video video = videoService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));

        // Добавляем просмотр
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName())
                    .orElse(null);
            if (user != null) {
                videoViewService.addView(user.getId(), id);
            }
        }

        model.addAttribute("video", video);
        model.addAttribute("comments", commentService.getCommentsByVideoId(id));
        model.addAttribute("likeCount", videoLikeService.getLikeCount(id));
        model.addAttribute("dislikeCount", videoLikeService.getDislikeCount(id));
        model.addAttribute("viewCount", videoViewService.getViewCount(id));

        // Устанавливаем значения по умолчанию для неаутентифицированных пользователей
        model.addAttribute("isOwnProfile", false);
        model.addAttribute("isOwner", false);
        model.addAttribute("isSubscribed", false);
        model.addAttribute("currentUsername", null);

        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("currentUsername", username);
            User user = userService.findByUsername(username)
                    .orElse(null);
            if (user != null) {
                model.addAttribute("userLikeStatus", videoLikeService.getUserLikeStatus(user.getId(), id));
                boolean isSubscribed = subscriptionService.isSubscribed(user.getId(), video.getAuthor().getId());
                boolean isOwner = user.getId().equals(video.getAuthor().getId());
                model.addAttribute("isSubscribed", isSubscribed);
                model.addAttribute("isOwner", isOwner);
                model.addAttribute("isOwnProfile", isOwner);
            }
        }

        return "videos/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                              Authentication authentication,
                              Model model) {
        Video video = videoService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!video.getAuthor().getId().equals(user.getId())) {
            return "redirect:/videos/" + id;
        }

        model.addAttribute("video", video);
        model.addAttribute("categories", categoryRepository.findAll());
        return "videos/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateVideo(@PathVariable Long id,
                             @RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        videoService.updateVideo(id, user.getId(), title, description, categoryId);
        redirectAttributes.addFlashAttribute("success", "Видео успешно обновлено!");
        return "redirect:/videos/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteVideo(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            videoService.deleteVideoWithAllRelations(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Видео успешно удалено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении видео: " + e.getMessage());
        }
        return "redirect:/videos";
    }

    @GetMapping("/{id}/video")
    @ResponseBody
    public ResponseEntity<Resource> serveVideo(@PathVariable Long id) {
        try {
            Video video = videoService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));

            // Получаем необходимые данные до закрытия транзакции
            String videoPath = video.getVideoPath();
            String title = video.getTitle();

            Path filePath = fileStorageService.loadFile(videoPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + title + "\"")
                        .body(resource);
            } else {
                throw new ResourceNotFoundException("Video file not found");
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error loading video: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/preview")
    @ResponseBody
    public ResponseEntity<Resource> servePreview(@PathVariable Long id) {
        try {
            Video video = videoService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));

            // Получаем необходимые данные до закрытия транзакции
            String previewPath = video.getPreviewPath();
            if (previewPath == null) {
                throw new ResourceNotFoundException("Preview not found");
            }
            String title = video.getTitle();

            Path filePath = fileStorageService.loadFile(previewPath);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + title + "\"")
                        .body(resource);
            } else {
                throw new ResourceNotFoundException("Preview file not found");
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error loading preview: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable Long id,
                                       @RequestParam boolean isLike,
                                       Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        videoLikeService.toggleLike(user.getId(), id, isLike);
        return ResponseEntity.ok().build();
    }
}

