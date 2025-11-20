package com.my.study_tg_bot.service.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Component
public class AnswerMethodFactory {

    public SendMessage getSendMessage(
            Long chatId,
            String text,
            ReplyKeyboard keyboard
    ){
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
    }

    public EditMessageText getEditMessageText(
            CallbackQuery callbackQuery,
            String text,
            InlineKeyboardMarkup keyboard
    ){
        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(text)
                .disableWebPagePreview(true)
                .replyMarkup(keyboard)
                .build();
    }

    public DeleteMessage getDeleteMessageText(Long chatId, Integer messageId){
        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }
}
