package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.r_mavlyutov.JStrimix.dao.CommentRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Comment;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.service.VideoServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock VideoRepository videoRepository;
    @Mock CommentRepository commentRepository;

    @InjectMocks
    VideoServiceImpl service;

    private static User stubAuthor(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("author");
        return u;
    }

    private static Video stubVideo(User author) {
        Video v = new Video();
        v.setId(777L);
        v.setAuthor(author);
        v.setTitle("t");
        v.setDescription("d");
        v.setVideoPath("/v.mp4");
        v.setPreviewPath("/p.png");
        return v;
    }

    @Nested
    @DisplayName("createVideoWithFirstComment")
    class CreateVideo {

        @Test
        @DisplayName("успех: создаёт видео и первый комментарий")
        void createsVideoAndFirstComment() {
            // given
            Long authorId = 1L;
            when(userRepository.findById(authorId)).thenReturn(Optional.of(stubAuthor(authorId)));
            // имитируем, что save вернул сущность с id
            when(videoRepository.save(any(Video.class)))
                    .thenAnswer(inv -> {
                        Video v = inv.getArgument(0);
                        v.setId(10L);
                        return v;
                    });

            // when
            Video saved = service.createVideoWithFirstComment(
                    authorId, "Title", "Desc", "/video.mp4", "/preview.png", "First!"
            );

            // then
            assertThat(saved.getId()).isEqualTo(10L);
            verify(userRepository).findById(authorId);
            verify(videoRepository).save(any(Video.class));
            verify(commentRepository).save(any(Comment.class));
            verifyNoMoreInteractions(userRepository, videoRepository, commentRepository);
        }

        @Test
        @DisplayName("успех: не создаёт комментарий если сообщение пустое/blank")
        void doesNotCreateCommentWhenBlank() {
            Long authorId = 1L;
            when(userRepository.findById(authorId)).thenReturn(Optional.of(stubAuthor(authorId)));
            when(videoRepository.save(any(Video.class)))
                    .thenAnswer(inv -> {
                        Video v = inv.getArgument(0);
                        v.setId(11L);
                        return v;
                    });

            Video saved = service.createVideoWithFirstComment(
                    authorId, "T", "D", "/v.mp4", "/p.png", "   "
            );

            assertThat(saved.getId()).isEqualTo(11L);
            verify(videoRepository).save(any(Video.class));
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("ошибка: автор не найден")
        void throwsWhenAuthorMissing() {
            Long authorId = 99L;
            when(userRepository.findById(authorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createVideoWithFirstComment(
                    authorId, "T", "D", "/v.mp4", "/p.png", "Hi"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Author not found");

            verify(videoRepository, never()).save(any());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("исключение при сохранении комментария пробрасывается наружу (транзакция должна откатиться)")
        void exceptionDuringCommentSavePropagates() {
            Long authorId = 1L;
            when(userRepository.findById(authorId)).thenReturn(Optional.of(stubAuthor(authorId)));
            when(videoRepository.save(any(Video.class)))
                    .thenAnswer(inv -> {
                        Video v = inv.getArgument(0);
                        v.setId(12L);
                        return v;
                    });
            when(commentRepository.save(any(Comment.class)))
                    .thenThrow(new RuntimeException("DB down"));

            assertThatThrownBy(() -> service.createVideoWithFirstComment(
                    authorId, "T", "D", "/v.mp4", "/p.png", "First"
            )).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB down");

            // verify calls
            verify(userRepository).findById(authorId);
            verify(videoRepository).save(any(Video.class));
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("deleteVideoWithAllRelations")
    class DeleteVideo {

        @Test
        @DisplayName("успех: удаляет комментарии, затем видео (порядок важен)")
        void deletesCommentsThenVideo() {
            Long videoId = 5L;
            User a = stubAuthor(1L);
            Video v = stubVideo(a);
            v.setId(videoId);

            when(videoRepository.findById(videoId)).thenReturn(Optional.of(v));
            when(commentRepository.findByVideo_Id(videoId))
                    .thenReturn(List.of(new Comment(), new Comment()));

            service.deleteVideoWithAllRelations(videoId);

            InOrder inOrder = inOrder(videoRepository, commentRepository);
            inOrder.verify(videoRepository).findById(videoId);
            inOrder.verify(commentRepository).findByVideo_Id(videoId);
            inOrder.verify(commentRepository).deleteAll(anyList());
            inOrder.verify(videoRepository).delete(v);

            verifyNoMoreInteractions(videoRepository, commentRepository);
        }

        @Test
        @DisplayName("ошибка: видео не найдено")
        void throwsWhenVideoMissing() {
            Long videoId = 404L;
            when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteVideoWithAllRelations(videoId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Video not found");

            verify(commentRepository, never()).findByVideo_Id(any());
            verify(commentRepository, never()).deleteAll(anyList());
            verify(videoRepository, never()).delete(any());
        }
    }
}
