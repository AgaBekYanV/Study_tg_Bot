package com.my.study_tg_bot.service.manager;

import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DefaultManager {

    final AnswerMethodFactory answerMethodFactory;

    @Autowired
    public DefaultManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    public SendMessage answerCommand(Message message){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Команда \"" + message.getText() + "\" не поддерживается",
                null
        );
    }
}
