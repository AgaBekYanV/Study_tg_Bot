package com.my.study_tg_bot.service.manager.help;

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

import static com.my.study_tg_bot.service.data.CallbackData.START;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class HelpManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public HelpManager(AnswerMethodFactory answerMethodFactory,
                       KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        log.info("Команда help. Принемает Message");
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        📍 Доступные команды:
                        Отображаются в панели "меню"
                        
                        📍 Доступные функции:
                        - Расписание (/timetable)
                        - Домашнее задание (/task)
                        - Контроль успеваемости (/progress)
                        """,
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Главное меню"),
                        List.of(1),
                        List.of(START)
                )
        );
    }



    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        log.info("CallbackQuery help. Принемает CallbackQuery");
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📍 Доступные команды:
                        Отображаются в панели "меню"
                        
                        📍 Доступные функции:
                        - Расписание (/timetable)
                        - Домашнее задание (/task)
                        - Контроль успеваемости (/progress)
                        """,
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("\uD83D\uDD19 Назад"),
                        List.of(1),
                        List.of(START)
                )
        );
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

}
