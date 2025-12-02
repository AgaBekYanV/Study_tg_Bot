package com.my.study_tg_bot.service.manager.auth;

import com.my.study_tg_bot.entity.user.Action;
import com.my.study_tg_bot.entity.user.Role;
import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.AbstractManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.AUTH_STUDENT;
import static com.my.study_tg_bot.service.data.CallbackData.AUTH_TEACHER;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AuthManager extends AbstractManager {

    final UserRepository userRepository;
    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;

    public AuthManager(UserRepository userRepository, AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.userRepository = userRepository;
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        Long chatId = message.getChatId();
        var user = userRepository.findById(chatId).orElseThrow();
        user.setAction(Action.AUTH);
        userRepository.save(user);
        return answerMethodFactory.getSendMessage(
                chatId,
                """
                        Выбирите свою роль""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Ученик", "Учитель"),
                        List.of(2),
                        List.of(AUTH_STUDENT,AUTH_TEACHER)
                )
        );
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        var user = userRepository.findById(chatId).orElseThrow();
        if (AUTH_TEACHER.equals(callbackQuery.getData())){
            user.setRole(Role.TEACHER);
        } else {
            user.setRole(Role.STUDENT);
        }
        user.setAction(Action.FREE);
        userRepository.save(user);

        try {
            bot.execute(answerMethodFactory.getAnswerCallbackQuery(
                    callbackQuery.getId(),
                    """
                            Авторизация прошла успешно, повторите предыдущий запрос"""
            ));

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return answerMethodFactory.getDeleteMessageText(
                chatId,
                messageId
        );
    }
}
