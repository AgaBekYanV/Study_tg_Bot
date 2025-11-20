package com.my.study_tg_bot.service.handler;

import com.my.study_tg_bot.service.manager.DefaultManager;
import com.my.study_tg_bot.service.manager.FeedbackManager;
import com.my.study_tg_bot.service.manager.HelpManager;
import com.my.study_tg_bot.service.manager.StartManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;


import static com.my.study_tg_bot.service.data.Command.*;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandHandler {
    final FeedbackManager  feedbackManager;
    final HelpManager helpManager;
    final DefaultManager defaultManager;
    final StartManager startManager;

    @Autowired
    public CommandHandler(
            FeedbackManager feedbackManager,
            HelpManager helpManager,
            DefaultManager defaultManager,
            StartManager startManager
    ) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
        this.defaultManager = defaultManager;
        this.startManager = startManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot){
        String command = message.getText();
        switch(command){
            case START -> {return startManager.answerCommand(message); }
            case FEEDBACK_COMMAND -> { return feedbackManager.answerCommand(message); }
            case HELP_COMMAND -> { return helpManager.answerCommand(message); }
            default -> {return defaultManager.answerCommand(message);}
        }
    }
}
