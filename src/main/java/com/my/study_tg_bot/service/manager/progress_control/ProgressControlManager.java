package com.my.study_tg_bot.service.manager.progress_control;

import com.my.study_tg_bot.entity.task.CompleteStatus;
import com.my.study_tg_bot.entity.user.User;
import com.my.study_tg_bot.repository.TaskRepository;
import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.AbstractManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressControlManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;
    final TaskRepository taskRepository;

    @Autowired
    public ProgressControlManager(AnswerMethodFactory answerMethodFactory,
                                  KeyboardFactory keyboardFactory,
                                  UserRepository userRepository,
                                  TaskRepository taskRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }


    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String keyWord = callbackQuery.getData();
        switch (keyWord) {
            case PROGRESS -> {return mainMenu(callbackQuery);}
            case PROGRESS_STAT -> {return stat(callbackQuery);}

        }
        String[] splitCallbackData = keyWord.split("_");
        switch (splitCallbackData[1]) {
            case USER -> {
                return showUserStat(callbackQuery, splitCallbackData[2]);
            }
        }
        return null;
    }

    private BotApiMethod<?> showUserStat(CallbackQuery callbackQuery, String id) {
        var student = userRepository.findUserByToken(id);
        var details = student.getUserDetails();
        StringBuilder text = new StringBuilder("\uD83D\uDD39Статистика по пользователю \"")
                .append(details.getFirstName())
                .append("\"")
                .append("\n\n");
        int success = taskRepository.countAllByUsersContainingAndIsFinishedAndCompleteStatus(
                student, true, CompleteStatus.SUCCESS
        );
        int fail = taskRepository.countAllByUsersContainingAndIsFinishedAndCompleteStatus(
                student, true, CompleteStatus.FAIL
        );
        int sum = fail + success;
        text.append("\uD83D\uDCCDРешено - ")
                .append(success);
        text.append("\n\uD83D\uDCCDПровалено - ")
                .append(fail);
        text.append("\n\uD83D\uDCCDВсего - ")
                .append(sum);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );


    }


    private BotApiMethod<?> mainMenu(Message message){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                     Здесь вы можете увидеть""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                      Здесь вы можете увидеть""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> stat(CallbackQuery callbackQuery){
        var teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        List<User> students = teacher.getUsers();
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        int index = 0;
        for (User student: students) {
            text.add(student.getUserDetails().getFirstName());
            data.add(PROGRESS_USER + student.getToken());
            if (index == 4) {
                cfg.add(index);
                index = 0;
            } else {
                index++;
            }
        }
        if (index != 0) {
            cfg.add(index);
        }
        data.add(PROGRESS);
        text.add("Назад");
        cfg.add(1);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Выберете ученика",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                )
        );
    }
}
