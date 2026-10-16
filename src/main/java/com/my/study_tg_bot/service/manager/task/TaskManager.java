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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
            case SENDING_TASK -> {
                try {
                    return addTask(message, chatId, user, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
            case SENDING_TEXT -> {
                try {
                    return editText(message, chatId, user, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
            case SENDING_MEDIA -> {
                try {
                    return editMedia(message, chatId, user, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
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
        if(MENU.equals(splitCallbackData[1])){
            return menu(callbackQuery, splitCallbackData[2]);
        }
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
                case TEXT -> {return askText(callbackQuery, splitCallbackData[3]);}
                case MEDIA -> {return askMedia(callbackQuery, splitCallbackData[3]);}
                case SEND -> {return askConfirmation(callbackQuery, splitCallbackData[3]);}
                case CONFIRM -> {
                    try {
                        return sendTask(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                case CHANGE -> {return askUser(callbackQuery, splitCallbackData[4]);}
                case STUDENT -> {return editStudent(callbackQuery, splitCallbackData[3]);}
                case SUCCESS -> {
                    try {
                        return sendInfo(callbackQuery, splitCallbackData[3], bot, true);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
                case FAIL -> {
                    try {
                        return sendInfo(callbackQuery, splitCallbackData[3], bot, false);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return null;
    }




    private BotApiMethod<?> sendInfo(CallbackQuery callbackQuery, String id, Bot bot, boolean status) throws TelegramApiException {
        var task = taskRepository.findById(UUID.fromString(id)).orElseThrow();
        var teacher = task.getTeacher();
        String studentName = task.getStudent().getUserDetails().getFirstName();
        bot.execute(
                answerMethodFactory.getEditMessageReplyMarkup(
                        callbackQuery, null
                )
        );

        if(status){
            return answerMethodFactory.getSendMessage(
                        teacher.getChatId(),
                        "Ученик " + studentName + " успешно выполнил(а) задания",
                        null
            );
        } else{
            return answerMethodFactory.getSendMessage(
                    teacher.getChatId(),
                    "Ученик " + studentName + " не справился(ась) с заданием",
                    null
            );
        }
    }

    private BotApiMethod<?> editStudent(CallbackQuery callbackQuery, String userId) {
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(
                userRepository.findUserByChatId(callbackQuery.getMessage().getChatId()), true
        );
        var student = userRepository.findUserByChatId(Long.valueOf(userId));
        task.changeUser(student);
        taskRepository.save(task);
        return menu(callbackQuery, String.valueOf(task.getId()));
    }

    private BotApiMethod<?> askUser(CallbackQuery callbackQuery, String id) {
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        var teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        int index = 0;
        for(User student: teacher.getUsers()){
            text.add(student.getUserDetails().getFirstName());
            data.add(TASK_CREATE_STUDENT + student.getChatId());
            if(index == 4){
                cfg.add(index);
                index = 0;
            } else {
                index++;
            }
        }
        if(index != 0){
            cfg.add(index);
        }
        data.add(TASK_MENU + id);
        text.add("Назад");
        cfg.add(1);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        👤 Измените получателя, которому хотите дать домашнее задание""",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                )
        );
    }

    private BotApiMethod<?> sendTask(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        var user = userRepository.findUserByChatId(chatId);
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);

        bot.execute(
                answerMethodFactory.getCopyMessage(
                        task.getStudent().getChatId(),
                        chatId,
                        task.getMessageId(),
                        keyboardFactory.getInlineKeyboardMarkup(
                                List.of("Готово", "Затрудняюсь с ответом"),
                                List.of(1, 1),
                                List.of(TASK_ANSWER_SUCCESS + id, TASK_ANSWER_FAIL + id)
                        )
                )
        );

        bot.execute(answerMethodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(),
                "Задание успешно отправлено"
        ));

        bot.execute(answerMethodFactory.getDeleteMessageText(
                chatId,task.getMessageId()
        ));

        task.setIsInCreation(false);
        taskRepository.save(task);
        return answerMethodFactory.getDeleteMessageText(
                chatId,
                task.getMenuId()
        );
    }

    private BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String id) {
        return answerMethodFactory.getEditMessageText(
          callbackQuery,
                "Подтвердить, что хотите отправить задание выше ученику",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Да", "Нет"),
                        List.of(2),
                        List.of(TASK_CREATE_CONFIRM + id, TASK_MENU + id)
                )
        );
    }

    private BotApiMethod<?> editMedia(Message message, Long chatId, User user, Bot bot) throws TelegramApiException {
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        if (message.hasText()) {
            return answerMethodFactory.getSendMessage(
                    chatId,
                    "Сообщение должно содержать один медиа файл (Фото, Видео, Документ или Аудио)\n\n" +
                            "И не должно содержать текста",
                    keyboardFactory.getInlineKeyboardMarkup(
                            List.of("Назад"),
                            List.of(1),
                            List.of(TASK_MENU + task.getId())
                    )
            );
        }
        task.setHasMedia(true);
        int previousId = task.getMessageId();
        task.setMessageId(Math.toIntExact(bot.execute(
                answerMethodFactory.getCopyMessage(
                        chatId,
                        chatId,
                        message.getMessageId()
                )
        ).getMessageId()));
        if (task.getTextContent() != null && !task.getTextContent().isEmpty()) {
            bot.execute(answerMethodFactory.getEditMessageCaption(
                    chatId, task.getMessageId(), task.getTextContent()
            ));
        }
        bot.execute(answerMethodFactory.getDeleteMessageText(
                chatId, previousId
        ));
        task.setMenuId(bot.execute(menu(message, user)).getMessageId());
        taskRepository.save(task);
        return null;
    }

    private BotApiMethod<?> editText(Message message, Long chatId, User user, Bot bot) throws TelegramApiException {
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        if(!message.hasText()){
            return answerMethodFactory.getSendMessage(
                    chatId,
                    "Сообщение должно содержать текст",
                    keyboardFactory.getInlineKeyboardMarkup(
                            List.of("Назад"),
                            List.of(1),
                            List.of(TASK_MENU + task.getId())
                    )
            );
        }
        String messageText = message.getText();
        task.setTextContent(messageText);
        int previousId = task.getMessageId();
        if(task.getHasMedia()){
            bot.execute(
                    answerMethodFactory.getEditMessageCaption(
                            chatId, previousId, messageText
                    )
            );
        } else{
            bot.execute(answerMethodFactory.getEditMessageText(
                    chatId,previousId,messageText
            ));
        }
        task.setMessageId(Math.toIntExact(bot.execute(
                answerMethodFactory.getCopyMessage(
                        chatId, chatId, previousId
                )
        ).getMessageId()));

        bot.execute(answerMethodFactory.getDeleteMessageText(
                chatId, previousId
        ));
        task.setMenuId(bot.execute(menu(message, user)).getMessageId());
        taskRepository.save(task);
        return null;
    }

    private BotApiMethod<?> askMedia(CallbackQuery callbackQuery, String id) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_MEDIA);
        userRepository.save(user);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Отправьте измененное Фото|Видео|Документ|Аудио",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_MENU + id)
                ));
    }

    private BotApiMethod<?> askText(CallbackQuery callbackQuery, String id) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_TASK);
        userRepository.save(user);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Отправьте измененный текст",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_MENU + id)
                )
        );
    }

    private BotApiMethod<?> menu(CallbackQuery callbackQuery, String id) {
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2,1,2),
                        List.of(TASK_CREATE_TEXT + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id, TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)
                )
        );
    }

    private SendMessage menu(Message message, User user) {
        user.setAction(Action.FREE);
        userRepository.save(user);
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        String id = String.valueOf(task.getId());
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 2),
                        List.of(TASK_CREATE_TEXT + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id, TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)
                )
        );
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

    private BotApiMethod<?> addTask(Message message, Long chatId, User user, Bot bot) throws TelegramApiException {
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        task.setMessageId(Math.toIntExact(bot.execute(
                answerMethodFactory.getCopyMessage(
                        chatId, chatId, message.getMessageId()
                )
        ).getMessageId()));
        if (message.hasVideo() || message.hasPhoto() || message.hasDocument() || message.hasAudio()) {
            task.setHasMedia(true);
        } else {
            task.setHasMedia(false);
        }
        if (message.hasText()) {
            task.setTextContent(message.getText());
        }
        String id = String.valueOf(task.getId());
        user.setAction(Action.FREE);
        userRepository.save(user);
        task.setMenuId(bot.execute(answerMethodFactory.getSendMessage(
                chatId,
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 2),
                        List.of(TASK_CREATE_TEXT + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id, TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)
                )
        )).getMessageId());
        taskRepository.save(task);
        return null;
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
                index = 0;
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
