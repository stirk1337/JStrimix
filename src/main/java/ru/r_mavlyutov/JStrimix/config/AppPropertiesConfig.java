package ru.r_mavlyutov.JStrimix.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppPropertiesConfig {

    @Value("${app.name:JStrimix}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @PostConstruct
    public void printAppInfo() {
        System.out.println("=== " + appName + " v" + appVersion + " ===");
        System.out.println("Видеохостинг успешно запущен!");
    }
}