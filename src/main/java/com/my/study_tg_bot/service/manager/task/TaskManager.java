package com.my.study_tg_bot.service.manager.task;

import com.my.study_tg_bot.entity.task.Task;
import com.my.study_tg_bot.entity.user.Action;
import com.my.study_tg_bot.entity.user.User;
import com.my.study_tg_bot.repository.TaskRepository;
import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.AbstractManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.my.study_tg_bot.service.data.CallbackData.*;
import static com.my.study_tg_bot.service.data.CallbackData.TIMETABLE_ADD;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;
    final TaskRepository taskRepository;

    @Autowired
    public TaskManager(
            AnswerMethodFactory answerMethodFactory,
            KeyboardFactory keyboardFactory,
            UserRepository userRepository,
            TaskRepository taskRepository
    ) {
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
        Long chatId = message.getChatId();
        var user = userRepository.findUserByChatId(chatId);
        try {
            bot.execute(answerMethodFactory.getDeleteMessageText(chatId, message.getMessageId()-1));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        switch (user.getAction()){
            case SENDING_TASK -> {return addTask(message, chatId, user);}
        }
        return null;
    }



    @Transactional
    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case TASK -> { return mainMenu(callbackQuery);}
            case TASK_CREATE -> {return create(callbackQuery);}
        }
        String[] splitCallbackData = callbackData.split("_");
        if(splitCallbackData.length > 2){
            String keyWord = splitCallbackData[2];
            switch (keyWord) {
                case USER -> {return setUser(callbackQuery, splitCallbackData);}
                case CANCEL -> {
                    try {
                        return abortCreation(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> abortCreation(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        taskRepository.deleteById(UUID.fromString(id));
        bot.execute(
                answerMethodFactory.getAnswerCallbackQuery(
                        callbackQuery.getId(),
                        "Операция успешно отменена"
                )
        );
        return answerMethodFactory.getDeleteMessageText(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> addTask(Message message, Long chatId, User user) {
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user,true);
        task.setMessageId(message.getMessageId());
        taskRepository.save(task);
        String id = String.valueOf(task.getId());

        user.setAction(Action.FREE);
        userRepository.save(user);
        return answerMethodFactory.getSendMessage(
                chatId,
                "Настройте ваше задание, когда будете готовы - жмите \"Отправить\"",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика","Отправить", "Отмена"),
                        List.of(2,1,2),
                        List.of(TASK_CREATE_TEXT + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id, TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)
                )
        );
    }

    private BotApiMethod<?> setUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
       var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        taskRepository.deleteByUsersContainingAndIsInCreation(user,true);
        taskRepository.save(Task.builder()
                .users(List.of(
                        userRepository.findUserByChatId(Long.valueOf(splitCallbackData[3])),
                        user
                ))
                .isInCreation(true)
                .build());
        user.setAction(Action.SENDING_TASK);
        userRepository.save(user);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        Отправьте задание одним сообщением, в дальнейшем, вы сможете его изменить""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }



    private BotApiMethod<?> create(CallbackQuery callbackQuery){
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();

        var teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        int index = 0;
        for(User student: teacher.getUsers()){
            text.add(student.getUserDetails().getFirstName());
            data.add(TASK_CREATE_USER + student.getChatId());
            if(index == 4){
                cfg.add(index);
            } else {
                index++;
            }
        }
        if(index != 0){
            cfg.add(index);
        }


        text.add("Назад");
        cfg.add(1);
        data.add(TASK);

        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание""",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                )
        );
    }

    private BotApiMethod<?> mainMenu(Message message){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                      🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                       🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                )
        );
    }
}
