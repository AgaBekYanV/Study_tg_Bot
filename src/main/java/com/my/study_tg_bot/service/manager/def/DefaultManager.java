package com.my.study_tg_bot.service.manager.def;

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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class DefaultManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;

    @Autowired
    public DefaultManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    @Override
    public SendMessage answerCommand(Message message, Bot bot){
        log.info("Формирую ответ на не известную команду \"{}\"", message.getText());
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Команда \"" + message.getText() + "\" не поддерживается",
                null
        );
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }


}
