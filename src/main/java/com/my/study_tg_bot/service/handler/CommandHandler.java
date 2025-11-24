package com.my.study_tg_bot.service.handler;

import com.my.study_tg_bot.service.manager.def.DefaultManager;
import com.my.study_tg_bot.service.manager.feedback.FeedbackManager;
import com.my.study_tg_bot.service.manager.help.HelpManager;
import com.my.study_tg_bot.service.manager.start.StartManager;
import com.my.study_tg_bot.service.manager.task.TaskManager;
import com.my.study_tg_bot.service.manager.timetable.TimetableManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;


import static com.my.study_tg_bot.service.data.Command.*;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CommandHandler {
    final FeedbackManager  feedbackManager;
    final HelpManager helpManager;
    final DefaultManager defaultManager;
    final StartManager startManager;
    final TimetableManager timetableManager;
    final TaskManager taskManager;

    @Autowired
    public CommandHandler(
            FeedbackManager feedbackManager,
            HelpManager helpManager,
            DefaultManager defaultManager,
            StartManager startManager,
            TimetableManager timetableManager,
            TaskManager taskManager
    ) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
        this.defaultManager = defaultManager;
        this.startManager = startManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot){
        String command = message.getText();
        log.info("Определяю команду. Передана команда {}", command);
        switch(command){
            case START -> {return startManager.answerCommand(message, bot); }
            case FEEDBACK_COMMAND -> { return feedbackManager.answerCommand(message, bot); }
            case HELP_COMMAND -> { return helpManager.answerCommand(message, bot); }
            case TIMETABLE -> { return timetableManager.answerCommand(message, bot); }
            case TASK -> { return taskManager.answerCommand(message, bot); }
            default -> {return defaultManager.answerCommand(message, bot);}
        }
    }
}
