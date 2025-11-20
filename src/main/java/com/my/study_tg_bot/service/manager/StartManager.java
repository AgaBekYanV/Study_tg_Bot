package com.my.study_tg_bot.service.manager;

import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.FEEDBACK;
import static com.my.study_tg_bot.service.data.CallbackData.HELP;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StartManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public StartManager(
            AnswerMethodFactory answerMethodFactory,
            KeyboardFactory keyboardFactory
    ) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    public SendMessage answerCommand(Message message){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        🖖Приветствую в Study Helper Bot, инструменте для упрощения взаимодействия репититора и ученика.
                                                
                        Что бот умеет?
                        📌 Составлять расписание
                        📌 Прикреплять домашние задания
                        📌 Ввести контроль успеваемости
                        """,
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Помощь", "Обратная связь"),
                        List.of(2),
                        List.of(HELP,FEEDBACK)
                )
        );
    }
}
