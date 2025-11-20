package com.my.study_tg_bot.service.handler;

import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.FeedbackManager;
import com.my.study_tg_bot.service.manager.HelpManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.FEEDBACK;
import static com.my.study_tg_bot.service.data.CallbackData.HELP;
import static com.my.study_tg_bot.service.data.Command.*;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandHandler {
    final KeyboardFactory keyboardFactory;
    final FeedbackManager  feedbackManager;
    final HelpManager helpManager;

    @Autowired
    public CommandHandler(KeyboardFactory keyboardFactory, FeedbackManager feedbackManager, HelpManager helpManager) {
        this.keyboardFactory = keyboardFactory;
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot){
        String command = message.getText();
        switch(command){
            case START -> { return start(message); }
            case FEEDBACK_COMMAND -> { return feedbackManager.answerCommand(message); }
            case HELP_COMMAND -> { return helpManager.answerCommand(message); }
            default -> {defaultAnswer(message);}
        }
        return null;
    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Команда \"" + message.getText() + "\" не поддерживается")
                .build();
    }


    private BotApiMethod<?> start(Message message){
        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Помощь", "Обратная связь"),
                        List.of(2),
                        List.of(HELP,FEEDBACK)
                ))
                .text("""
                        🖖Приветствую в Study Helper Bot, инструменте для упрощения взаимодействия репититора и ученика.
                                                
                        Что бот умеет?
                        📌 Составлять расписание
                        📌 Прикреплять домашние задания
                        📌 Ввести контроль успеваемости
                        """)
                .build();
    }
}
