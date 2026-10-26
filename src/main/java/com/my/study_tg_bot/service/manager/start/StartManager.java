package com.my.study_tg_bot.service.manager.start;

import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.AbstractManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.FEEDBACK;
import static com.my.study_tg_bot.service.data.CallbackData.HELP;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class StartManager extends AbstractManager {

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

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot){ //был тип данных SendMessage. Проверить работу как появится интернет
        log.info("Формирую ответ на команду start");
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

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
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
