package ru.r_mavlyutov.JStrimix.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.dao.custom.VideoRepositoryImpl;
import ru.r_mavlyutov.JStrimix.entity.Category;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = VideoCustomController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("REST Assured тесты для VideoCustomController")
class VideoCustomControllerRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoRepository videoRepository;

    @MockBean
    private VideoRepositoryImpl videoRepositoryCustom;

    private Video testVideo;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        // Подготовка тестовых данных
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRoles("USER");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Music");

        testVideo = new Video();
        testVideo.setId(1L);
        testVideo.setTitle("Test Video");
        testVideo.setDescription("Test Description");
        testVideo.setVideoPath("/path/to/video");
        testVideo.setPreviewPath("/path/to/preview");
        testVideo.setAuthor(testUser);
        testVideo.setCategory(testCategory);
        testVideo.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("GET /videos/custom/byId - успешное получение видео по ID")
    void testGetById_Success() {
        // Arrange
        Long videoId = 1L;
        when(videoRepository.findById(videoId)).thenReturn(Optional.of(testVideo));

        // Act & Assert
        given()
                .param("id", videoId)
        .when()
                .get("/videos/custom/byId")
        .then()
                .statusCode(200)
                .body("id", equalTo(videoId.intValue()))
                .body("title", equalTo("Test Video"))
                .body("description", equalTo("Test Description"))
                .body("videoPath", equalTo("/path/to/video"));
    }

    @Test
    @DisplayName("GET /videos/custom/byId - видео не найдено (404)")
    void testGetById_NotFound() {
        // Arrange
        Long videoId = 999L;
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        given()
                .param("id", videoId)
        .when()
                .get("/videos/custom/byId")
        .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"))
                .body("message", containsString("Video with id " + videoId + " not found"));
    }

    @Test
    @DisplayName("GET /videos/custom/byId - невалидный параметр ID (400)")
    void testGetById_InvalidParameter() {
        // Act & Assert
        given()
                .param("id", "invalid")
        .when()
                .get("/videos/custom/byId")
        .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("GET /videos/custom/byCategory - успешное получение видео по категории")
    void testGetByCategory_Success() {
        // Arrange
        String categoryName = "Music";
        List<Video> videos = new ArrayList<>();
        videos.add(testVideo);

        when(videoRepositoryCustom.findByCategoryNameCriteria(categoryName)).thenReturn(videos);

        // Act & Assert
        given()
                .param("categoryName", categoryName)
        .when()
                .get("/videos/custom/byCategory")
        .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].title", equalTo("Test Video"))
                .body("[0].category.name", equalTo("Music"));
    }

    @Test
    @DisplayName("GET /videos/custom/byCategory - категория не найдена (пустой список)")
    void testGetByCategory_EmptyList() {
        // Arrange
        String categoryName = "NonExistent";
        when(videoRepositoryCustom.findByCategoryNameCriteria(categoryName)).thenReturn(new ArrayList<>());

        // Act & Assert
        given()
                .param("categoryName", categoryName)
        .when()
                .get("/videos/custom/byCategory")
        .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("GET /videos/custom/byCategory - отсутствует обязательный параметр")
    void testGetByCategory_MissingParameter() {
        // Act & Assert
        given()
        .when()
                .get("/videos/custom/byCategory")
        .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("GET /videos/custom/byAuthor - успешное получение видео по автору и периоду")
    void testGetByAuthorAndPeriod_Success() {
        // Arrange
        String username = "testuser";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now().plusSeconds(3600);
        List<Video> videos = new ArrayList<>();
        videos.add(testVideo);

        when(videoRepositoryCustom.findByAuthorUsernameAndCreatedAtBetweenCriteria(
                anyString(), any(Instant.class), any(Instant.class)
        )).thenReturn(videos);

        // Act & Assert
        given()
                .param("username", username)
                .param("from", from.toString())
                .param("to", to.toString())
        .when()
                .get("/videos/custom/byAuthor")
        .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].title", equalTo("Test Video"));
    }

    @Test
    @DisplayName("GET /videos/custom/byAuthor - без параметров периода")
    void testGetByAuthorAndPeriod_WithoutPeriod() {
        // Arrange
        String username = "testuser";
        List<Video> videos = new ArrayList<>();
        videos.add(testVideo);

        when(videoRepositoryCustom.findByAuthorUsernameAndCreatedAtBetweenCriteria(
                anyString(), any(), any()
        )).thenReturn(videos);

        // Act & Assert
        given()
                .param("username", username)
        .when()
                .get("/videos/custom/byAuthor")
        .then()
                .statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @DisplayName("GET /videos/custom/byAuthor - отсутствует обязательный параметр username")
    void testGetByAuthorAndPeriod_MissingUsername() {
        // Act & Assert
        given()
        .when()
                .get("/videos/custom/byAuthor")
        .then()
                .statusCode(500);
    }
}

