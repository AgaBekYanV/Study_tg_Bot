package com.my.study_tg_bot.service.manager.timetable;

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

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public TimetableManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
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
        String text = callbackQuery.getData();
        switch (text) {
            case TIMETABLE -> { return mainMenu(callbackQuery);}
            case TIMETABLE_SHOW -> {return show(callbackQuery);}
            case TIMETABLE_ADD -> {return add(callbackQuery);}
            case TIMETABLE_REMOVE -> {return remove(callbackQuery);}
        }
        return null;
    }

    private BotApiMethod<?> mainMenu(Message message){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Показать мое расписание",
                                "Удалить занятие", "Добавить занятие"),
                        List.of(1,2),
                        List.of(TIMETABLE_SHOW,TIMETABLE_REMOVE,TIMETABLE_ADD)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Показать мое расписание",
                                "Удалить занятие", "Добавить занятие"),
                        List.of(1,2),
                        List.of(TIMETABLE_SHOW,TIMETABLE_REMOVE,TIMETABLE_ADD)
                )
        );
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Выберете день недели""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)
                )
        );
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery){return answerMethodFactory.getEditMessageText(
            callbackQuery,
            """
                   ✏️ Выберете день, в который хотите добавить занятие:""",
            keyboardFactory.getInlineKeyboardMarkup(
                    List.of("Назад"),
                    List.of(1),
                    List.of(TIMETABLE)
            )
    );}

    private BotApiMethod<?> remove(CallbackQuery callbackQuery){return answerMethodFactory.getEditMessageText(
            callbackQuery,
            """
                    ✂️ Выберете занятие, которое хотите удалить из вашего расписания""",
            keyboardFactory.getInlineKeyboardMarkup(
                    List.of("Назад"),
                    List.of(1),
                    List.of(TIMETABLE)
            )
    );}
}
