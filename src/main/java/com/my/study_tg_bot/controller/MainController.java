package com.my.study_tg_bot.controller;

import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainController {

    final Bot bot;


    @Autowired
    public MainController(Bot bot) {
        this.bot = bot;

    }

    @PostMapping("/")
    public BotApiMethod<?> listener(@RequestBody Update update){
        return bot.onWebhookUpdateReceived(update);
    }

}
