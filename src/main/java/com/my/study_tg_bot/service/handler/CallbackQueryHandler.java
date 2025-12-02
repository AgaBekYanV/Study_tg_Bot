package com.my.study_tg_bot.service.handler;

import com.my.study_tg_bot.service.manager.auth.AuthManager;
import com.my.study_tg_bot.service.manager.feedback.FeedbackManager;
import com.my.study_tg_bot.service.manager.help.HelpManager;
import com.my.study_tg_bot.service.manager.progress_control.ProgressControlManager;
import com.my.study_tg_bot.service.manager.task.TaskManager;
import com.my.study_tg_bot.service.manager.timetable.TimetableManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.my.study_tg_bot.service.data.CallbackData.*;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackQueryHandler {

    final HelpManager helpManager;
    final FeedbackManager feedbackManager;
    final TimetableManager timetableManager;
    final TaskManager taskManager;
    final ProgressControlManager progressControlManager;
    final AuthManager authManager;

    @Autowired
    public CallbackQueryHandler(
            HelpManager helpManager,
            FeedbackManager feedbackManager,
            TimetableManager timetableManager,
            TaskManager taskManager,
            ProgressControlManager progressControlManager,
            AuthManager authManager
    ) {
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.progressControlManager = progressControlManager;
        this.authManager = authManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot){
        String callbackData = callbackQuery.getData();
        String keyWord = callbackData.split("_")[0];
        if(TIMETABLE.equals(keyWord)){
            return timetableManager.answerCallbackQuery(callbackQuery,bot);
        }
        if(TASK.equals(keyWord)){
            return taskManager.answerCallbackQuery(callbackQuery,bot);
        }
        if(PROGRESS.equals(keyWord)){
            return progressControlManager.answerCallbackQuery(callbackQuery,bot);
        }
        if(AUTH.equals(keyWord)){
            return authManager.answerCallbackQuery(callbackQuery,bot);
        }
        switch(callbackData){
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
        }
        return null;
    }
}
