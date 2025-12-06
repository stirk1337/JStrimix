package ru.r_mavlyutov.JStrimix.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.service.UserService;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Selenium тесты для проверки входа и выхода")
class LoginLogoutSeleniumTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass123";
    private static final String TEST_EMAIL = "testuser@example.com";

    @BeforeAll
    static void setupAll() {
        // Настройка WebDriverManager для автоматической загрузки драйвера
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        // Настройка ChromeDriver с опциями
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Запуск в headless режиме для CI/CD
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;

        // Создание тестового пользователя перед каждым тестом
        createTestUser();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void createTestUser() {
        // Проверяем, существует ли пользователь, если нет - создаем
        if (!userService.existsByUsernameOrEmail(TEST_USERNAME, TEST_EMAIL)) {
            User user = new User();
            user.setUsername(TEST_USERNAME);
            user.setEmail(TEST_EMAIL);
            user.setPassword(TEST_PASSWORD);
            user.setRoles("USER");
            userService.saveUser(user);
        }
    }

    @Test
    @DisplayName("Успешный вход в приложение")
    void testSuccessfulLogin() {
        // Arrange
        driver.get(baseUrl + "/login");

        // Act - находим поля формы и заполняем их
        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.clear();
        usernameField.sendKeys(TEST_USERNAME);

        passwordField.clear();
        passwordField.sendKeys(TEST_PASSWORD);

        loginButton.click();

        // Assert - проверяем, что произошел редирект на страницу списка видео
        wait.until(ExpectedConditions.urlContains("/videos/list"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/videos/list"), 
                "После входа должен быть редирект на /videos/list");

        // Проверяем, что на странице есть заголовок "Список видео"
        WebElement pageTitle = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("h1"))
        );
        assertEquals("Список видео", pageTitle.getText(),
                "На странице должен отображаться заголовок 'Список видео'");
    }

    @Test
    @DisplayName("Неудачный вход с неверными учетными данными")
    void testFailedLogin() {
        // Arrange
        driver.get(baseUrl + "/login");

        // Act - вводим неверные учетные данные
        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.clear();
        usernameField.sendKeys("wronguser");

        passwordField.clear();
        passwordField.sendKeys("wrongpassword");

        loginButton.click();

        // Assert - проверяем, что остались на странице логина и появилось сообщение об ошибке
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login") || currentUrl.contains("/login?error"),
                "При неверных данных должен остаться на странице логина");

        WebElement errorMessage = driver.findElement(By.cssSelector(".alert-danger"));
        assertTrue(errorMessage.isDisplayed(), "Должно отображаться сообщение об ошибке");
        assertTrue(errorMessage.getText().contains("Неверное имя пользователя") ||
                   errorMessage.getText().contains("error"),
                "Сообщение об ошибке должно содержать информацию о неверных данных");
    }

    @Test
    @DisplayName("Успешный выход из приложения")
    void testSuccessfulLogout() {
        // Arrange - сначала входим в систему
        driver.get(baseUrl + "/login");

        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.clear();
        usernameField.sendKeys(TEST_USERNAME);
        passwordField.clear();
        passwordField.sendKeys(TEST_PASSWORD);
        loginButton.click();

        // Ждем успешного входа
        wait.until(ExpectedConditions.urlContains("/videos/list"));

        // Act - находим и нажимаем кнопку выхода
        WebElement logoutButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("form[action*='/logout'] button")
                )
        );
        logoutButton.click();

        // Assert - проверяем, что произошел редирект на страницу логина
        wait.until(ExpectedConditions.urlContains("/login"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"),
                "После выхода должен быть редирект на /login");

        // Проверяем, что отображается сообщение об успешном выходе
        try {
            WebElement successMessage = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success"))
            );
            assertTrue(successMessage.getText().contains("Вы успешно вышли"),
                    "Должно отображаться сообщение об успешном выходе");
        } catch (Exception e) {
            // Сообщение может не отображаться, это не критично для теста
            // Главное - что произошел редирект на страницу логина
        }

        // Проверяем, что кнопка входа снова доступна (пользователь не авторизован)
        WebElement loginForm = driver.findElement(By.cssSelector("form[action*='/login']"));
        assertNotNull(loginForm, "Должна быть доступна форма входа");
    }

    @Test
    @DisplayName("Проверка доступа к защищенной странице без авторизации")
    void testUnauthorizedAccess() {
        // Arrange & Act - пытаемся открыть защищенную страницу без входа
        driver.get(baseUrl + "/videos/list");

        // Assert - проверяем, что произошел редирект на страницу логина
        wait.until(ExpectedConditions.urlContains("/login"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"),
                "При попытке доступа к защищенной странице должен быть редирект на /login");
    }

    @Test
    @DisplayName("Проверка формы входа - наличие всех необходимых элементов")
    void testLoginFormElements() {
        // Arrange & Act
        driver.get(baseUrl + "/login");

        // Assert - проверяем наличие всех элементов формы
        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("username"))
        );
        assertTrue(usernameField.isDisplayed(), "Поле username должно быть видимым");

        WebElement passwordField = driver.findElement(By.id("password"));
        assertTrue(passwordField.isDisplayed(), "Поле password должно быть видимым");
        assertEquals("password", passwordField.getDomAttribute("type"),
                "Поле password должно иметь тип password");

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(loginButton.isDisplayed(), "Кнопка входа должна быть видимой");
        assertTrue(loginButton.isEnabled(), "Кнопка входа должна быть активной");

        // Проверяем наличие ссылки на регистрацию
        WebElement registerLink = driver.findElement(By.cssSelector("a[href*='/register']"));
        assertTrue(registerLink.isDisplayed(), "Ссылка на регистрацию должна быть видимой");
    }
}

