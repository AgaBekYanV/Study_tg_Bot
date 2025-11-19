package com.my.study_tg_bot.controller;

import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.w3c.dom.ls.LSOutput;

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

//    private BotApiMethod<?> echo(Message message) {
//        System.out.println(message.getText());
//        return SendMessage.builder()
//                .chatId(message.getChatId())
//                .text(message.getText())
//                .build();
//    }
}
