package com.my.study_tg_bot;

import com.my.study_tg_bot.telegram.TelegramProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramProperties.class)
public class StudyTgBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyTgBotApplication.class, args);
    }

}
