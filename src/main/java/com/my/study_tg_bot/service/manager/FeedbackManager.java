package com.my.study_tg_bot.service.manager;

import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackManager {

    final AnswerMethodFactory answerMethodFactory;

    @Autowired
    public FeedbackManager(AnswerMethodFactory answerMethodFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }


    public BotApiMethod<?> answerCommand(Message message) {
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/AgaBekYanV
                        Telegram - https://t.me/slavavyaceslavu
                        """,
                null);
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/AgaBekYanV
                        Telegram - https://t.me/slavavyaceslavu
                        """,
                null);
    }
}
