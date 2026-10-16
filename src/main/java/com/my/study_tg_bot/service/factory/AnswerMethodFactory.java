package com.my.study_tg_bot.service.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.Serializable;

@Component
@Slf4j
public class AnswerMethodFactory {

    public SendMessage getSendMessage(
            Long chatId,
            String text,
            ReplyKeyboard keyboard
    ){
        log.info("Передаю каркас ответа на SendMessage.");
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
        log.info("Передаю каркас ответа на EditMessageText.");
        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(text)
                .disableWebPagePreview(true)
                .replyMarkup(keyboard)
                .build();
    }

    public EditMessageText getEditMessageText(Long chatId, Integer messageId, String text){
        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .disableWebPagePreview(true)
                .build();
    }

    

    public DeleteMessage getDeleteMessageText(Long chatId, Integer messageId){
        log.info("Передаю каркас ответа на DeleteMessage.");
        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public AnswerCallbackQuery getAnswerCallbackQuery(String callbackQueryId, String text){
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .build();
    }

    public EditMessageReplyMarkup getEditMessageReplyMarkup(CallbackQuery callbackQuery,
                                                            InlineKeyboardMarkup inlineKeyboardMarkup) {
        return EditMessageReplyMarkup.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    public CopyMessage getCopyMessage(Long chatId, Long fromChatId, Integer messageId) {
        return CopyMessage.builder()
                .fromChatId(fromChatId)
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public CopyMessage getCopyMessage(Long chatId, Long fromChatId, Integer messageId, ReplyKeyboard replyKeyboard) {
        return CopyMessage.builder()
                .fromChatId(fromChatId)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(replyKeyboard)
                .build();
    }

    public EditMessageCaption getEditMessageCaption(Long chatId, Integer messageId, String caption) {
        return EditMessageCaption.builder()
                .chatId(chatId)
                .caption(caption)
                .messageId(messageId)
                .build();
    }


}
