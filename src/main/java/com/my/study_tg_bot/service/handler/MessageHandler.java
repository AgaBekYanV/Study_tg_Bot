package com.my.study_tg_bot.service.handler;

import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.manager.search.SearchManager;
import com.my.study_tg_bot.service.manager.task.TaskManager;
import com.my.study_tg_bot.service.manager.timetable.TimetableManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageHandler {

    final SearchManager searchManager;
    final TimetableManager timetableManager;
    final UserRepository userRepository;
    final TaskManager taskManager;

    @Autowired
    public MessageHandler(SearchManager searchManager,
                          TimetableManager timetable,
                          UserRepository userRepository, TaskManager taskManager) {
        this.searchManager = searchManager;
        this.timetableManager = timetable;
        this.userRepository = userRepository;
        this.taskManager = taskManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot){
        var user = userRepository.findUserByChatId(message.getChatId());
        switch (user.getAction()){
            case SEND_TOKEN -> { return searchManager.answerMessage(message, bot);}
            case SENDING_DESCRIPTION, SENDING_TITLE -> { return timetableManager.answerMessage(message, bot);}
            case SENDING_TASK -> {return taskManager.answerMessage(message, bot);}
        }
        return null;
    }
}
