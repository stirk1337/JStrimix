package ru.r_mavlyutov.JStrimix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.r_mavlyutov.JStrimix.dao.CommentRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Comment;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для VideoServiceImpl")
class VideoServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private VideoServiceImpl videoService;

    private User testUser;
    private Video testVideo;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRoles("USER");

        testVideo = new Video();
        testVideo.setId(1L);
        testVideo.setTitle("Test Video");
        testVideo.setDescription("Test Description");
        testVideo.setVideoPath("/path/to/video");
        testVideo.setPreviewPath("/path/to/preview");
        testVideo.setAuthor(testUser);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setUser(testUser);
        testComment.setVideo(testVideo);
        testComment.setMessage("First comment");
    }

    @Test
    @DisplayName("Успешное создание видео с первым комментарием")
    void testCreateVideoWithFirstComment_Success() {
        // Arrange
        String title = "Test Video";
        String description = "Test Description";
        String videoPath = "/path/to/video";
        String previewPath = "/path/to/preview";
        String firstCommentMessage = "First comment";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(videoRepository.save(any(Video.class))).thenReturn(testVideo);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        Video result = videoService.createVideoWithFirstComment(
                1L, title, description, videoPath, previewPath, firstCommentMessage
        );

        // Assert
        assertNotNull(result);
        assertEquals(testVideo.getId(), result.getId());
        assertEquals(title, result.getTitle());
        verify(userRepository, times(1)).findById(1L);
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Успешное создание видео без комментария")
    void testCreateVideoWithFirstComment_WithoutComment() {
        // Arrange
        String title = "Test Video";
        String description = "Test Description";
        String videoPath = "/path/to/video";
        String previewPath = "/path/to/preview";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(videoRepository.save(any(Video.class))).thenReturn(testVideo);

        // Act
        Video result = videoService.createVideoWithFirstComment(
                1L, title, description, videoPath, previewPath, null
        );

        // Assert
        assertNotNull(result);
        assertEquals(testVideo.getId(), result.getId());
        verify(userRepository, times(1)).findById(1L);
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание видео с пустым комментарием - комментарий не создается")
    void testCreateVideoWithFirstComment_WithEmptyComment() {
        // Arrange
        String title = "Test Video";
        String description = "Test Description";
        String videoPath = "/path/to/video";
        String previewPath = "/path/to/preview";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(videoRepository.save(any(Video.class))).thenReturn(testVideo);

        // Act
        Video result = videoService.createVideoWithFirstComment(
                1L, title, description, videoPath, previewPath, "   "
        );

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(videoRepository, times(1)).save(any(Video.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание видео - исключение когда автор не найден")
    void testCreateVideoWithFirstComment_AuthorNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> videoService.createVideoWithFirstComment(
                        999L, "Title", "Description", "/path", "/preview", "Comment"
                )
        );

        assertEquals("Author not found: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(videoRepository, never()).save(any(Video.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Успешное удаление видео со всеми связанными данными")
    void testDeleteVideoWithAllRelations_Success() {
        // Arrange
        Long videoId = 1L;
        List<Comment> comments = new ArrayList<>();
        comments.add(testComment);

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(testVideo));
        when(commentRepository.findByVideo_Id(videoId)).thenReturn(comments);
        doNothing().when(commentRepository).deleteAll(comments);
        doNothing().when(videoRepository).delete(testVideo);

        // Act
        assertDoesNotThrow(() -> videoService.deleteVideoWithAllRelations(videoId));

        // Assert
        verify(videoRepository, times(1)).findById(videoId);
        verify(commentRepository, times(1)).findByVideo_Id(videoId);
        verify(commentRepository, times(1)).deleteAll(comments);
        verify(videoRepository, times(1)).delete(testVideo);
    }

    @Test
    @DisplayName("Удаление видео без комментариев")
    void testDeleteVideoWithAllRelations_NoComments() {
        // Arrange
        Long videoId = 1L;
        List<Comment> emptyComments = new ArrayList<>();

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(testVideo));
        when(commentRepository.findByVideo_Id(videoId)).thenReturn(emptyComments);
        doNothing().when(commentRepository).deleteAll(emptyComments);
        doNothing().when(videoRepository).delete(testVideo);

        // Act
        assertDoesNotThrow(() -> videoService.deleteVideoWithAllRelations(videoId));

        // Assert
        verify(videoRepository, times(1)).findById(videoId);
        verify(commentRepository, times(1)).findByVideo_Id(videoId);
        verify(commentRepository, times(1)).deleteAll(emptyComments);
        verify(videoRepository, times(1)).delete(testVideo);
    }

    @Test
    @DisplayName("Удаление видео - исключение когда видео не найдено")
    void testDeleteVideoWithAllRelations_VideoNotFound() {
        // Arrange
        Long videoId = 999L;
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> videoService.deleteVideoWithAllRelations(videoId)
        );

        assertEquals("Video not found: 999", exception.getMessage());
        verify(videoRepository, times(1)).findById(videoId);
        verify(commentRepository, never()).findByVideo_Id(anyLong());
        verify(commentRepository, never()).deleteAll(any());
        verify(videoRepository, never()).delete(any(Video.class));
    }
}

