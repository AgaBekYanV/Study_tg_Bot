package com.my.study_tg_bot.service.manager.task;

import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.AbstractManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.*;
import static com.my.study_tg_bot.service.data.CallbackData.TIMETABLE_ADD;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public TaskManager(
            AnswerMethodFactory answerMethodFactory,
                       KeyboardFactory keyboardFactory
    ) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }


    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case TASK -> { return mainMenu(callbackQuery);}
            case TASK_CREATE -> {return create(callbackQuery);}
        }
        return null;
    }

    private BotApiMethod<?> mainMenu(Message message){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                      🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                       🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }

    private BotApiMethod<?> create(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK)
                )
        );
    }
}
