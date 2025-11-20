package com.my.study_tg_bot.service.manager;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class HelpManager {

    public BotApiMethod<?> answerCommand(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости
                        """)
                .build();
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text("""
                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости
                        """)
                .build();
    }


}
