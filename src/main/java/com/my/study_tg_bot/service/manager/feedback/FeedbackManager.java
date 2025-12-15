package com.my.study_tg_bot.service.manager.feedback;

import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
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

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FeedbackManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;

    @Autowired
    public FeedbackManager(AnswerMethodFactory answerMethodFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        log.info("Команда feedback Message");
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/AgaBekYanV
                        Telegram - https://t.me/slavavyaceslavu
                        """,
                null);
    }


    @Override
    public BotApiMethod<?>  answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        log.info("CallbackQuery feedback");
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/AgaBekYanV
                        Telegram - https://t.me/slavavyaceslavu
                        """,
                null);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
