package ru.r_mavlyutov.JStrimix.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ru.r_mavlyutov.JStrimix.dao.CategoryRepository;
import ru.r_mavlyutov.JStrimix.entity.Category;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.service.UserService;

@Configuration
public class DataInitializer {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private CategoryRepository categoryRepository;

    @PostConstruct
    public void initializeData() {
        createAdminUser();
        createDefaultCategories();
    }

    private void createAdminUser() {
        if (userService != null && !userService.existsByUsernameOrEmail("admin", "admin@example.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("admin"); // будет захеширован через UserService
            admin.setRoles("ADMIN,USER");
            userService.saveUser(admin);
            System.out.println("✓ Создан администратор: username=admin, password=admin");
        }
    }

    private void createDefaultCategories() {
        if (categoryRepository != null) {
            String[] categories = {"Музыка", "Игры", "Образование", "Развлечения", "Спорт", "Технологии", "Кулинария", "Путешествия"};
            for (String categoryName : categories) {
                if (categoryRepository.findByName(categoryName).isEmpty()) {
                    Category category = new Category();
                    category.setName(categoryName);
                    categoryRepository.save(category);
                }
            }
            System.out.println("✓ Инициализированы категории");
        }
    }
}

