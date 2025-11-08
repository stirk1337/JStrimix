package ru.r_mavlyutov.JStrimix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Показать форму логина
     */
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        return "login";
    }

    /**
     * Показать форму регистрации
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Обработать регистрацию
     */
    @PostMapping("/register/process")
    public String processRegistration(@ModelAttribute User user, Model model) {
        // Проверка на существование пользователя
        if (userService.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
            model.addAttribute("error", "Пользователь с таким именем или email уже существует");
            model.addAttribute("user", user);
            return "register";
        }

        // Устанавливаем роль по умолчанию
        if (user.getRoles() == null || user.getRoles().isBlank()) {
            user.setRoles("USER");
        }

        // Сохраняем пользователя
        userService.saveUser(user);

        // Перенаправляем на страницу логина
        model.addAttribute("message", "Регистрация успешна! Войдите в систему.");
        return "login";
    }
}


