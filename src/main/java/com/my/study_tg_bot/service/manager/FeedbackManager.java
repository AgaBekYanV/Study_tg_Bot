package com.my.study_tg_bot.service.manager;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class FeedbackManager {

    public BotApiMethod<?> answerCommand(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/AgaBekYanV
                        Telegram - https://t.me/slavavyaceslavu
                        """)
                .build();
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text("""
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/AgaBekYanV
                        Telegram - https://t.me/slavavyaceslavu
                        """)
                .build();
    }
}
