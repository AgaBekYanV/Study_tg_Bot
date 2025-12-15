package com.my.study_tg_bot.service.manager.search;

import com.my.study_tg_bot.entity.user.Action;
import com.my.study_tg_bot.entity.user.Role;
import com.my.study_tg_bot.entity.user.User;
import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.factory.AnswerMethodFactory;
import com.my.study_tg_bot.service.factory.KeyboardFactory;
import com.my.study_tg_bot.service.manager.AbstractManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.SEARCH_CANCEL;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;

    @Autowired
    public SearchManager(AnswerMethodFactory answerMethodFactory,
                         KeyboardFactory keyboardFactory,
                         UserRepository userRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
    }


    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return askToken(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        try {
            bot.execute(answerMethodFactory.getDeleteMessageText(
                    message.getChatId(),
                    message.getMessageId() - 1
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        var user = userRepository.findUserByChatId(message.getChatId());
        switch (user.getAction()){
            case SEND_TOKEN -> {
                return checkToken(message, user);
            }
        }
        return null;
    }



    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        switch (callbackQuery.getData()){
            case SEARCH_CANCEL -> {
                try {
                    return cancel(callbackQuery, bot);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> checkToken(Message message,
                                       User user) {
        String token = message.getText();
        var user2 = userRepository.findUserByToken(token);
        if(user2 == null){
            return answerMethodFactory.getSendMessage(
                    message.getChatId(),
                    "По данному токену не найдено ни одного пользователя\n\nПовторите попытку!",
                    keyboardFactory.getInlineKeyboardMarkup(
                            List.of("Отмена операции"),
                            List.of(1),
                            List.of(SEARCH_CANCEL)
                    )
            );
        }
        if(validation(user,user2)){
            if(user.getRole() == Role.TEACHER){
                user.addUser(user2);
            } else {
                user2.addUser(user);
            }
            user.setAction(Action.FREE);
            userRepository.save(user);
            userRepository.save(user2);
            return answerMethodFactory.getSendMessage(message.getChatId(),
                    "Связь успешно установлена",
                    null
            );
        }
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Ошибка. Конфликт ролей. \n\nПовторите попытку!",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Отмена операции"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)
                )
        );

    }

    private BotApiMethod<?> cancel(CallbackQuery callbackQuery, Bot bot) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        var user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.FREE);
        userRepository.save(user);
        bot.execute(answerMethodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(),
                "Операция успешно отменена"

        ));


        return answerMethodFactory.getDeleteMessageText(
                chatId,
                callbackQuery.getMessage().getMessageId()
        );
    }

    private boolean validation(User user1, User user2){
        return user1.getRole() != user2.getRole();
    }

    private BotApiMethod<?> askToken(Message message){
        Long chatId = message.getChatId();
        var user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.SEND_TOKEN);
        userRepository.save(user);
        return answerMethodFactory.getSendMessage(
                chatId,
                "Отправьте токен ",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Отмена"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)
                )
        );
    }
}
