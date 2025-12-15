package com.my.study_tg_bot.service.manager.profile;

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

import java.util.List;

import static com.my.study_tg_bot.service.data.CallbackData.PROFILE_REFRESH_TOKEN;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;

    @Autowired
    public ProfileManager(AnswerMethodFactory answerMethodFactory,
                          KeyboardFactory keyboardFactory,
                          UserRepository userRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return showProfile(message);
    }



    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        switch (callbackQuery.getData()){
            case PROFILE_REFRESH_TOKEN -> {return refreshToken(callbackQuery);}
        }
        return null;
    }

    private BotApiMethod<?> showProfile(Message message) {
        Long chatId = message.getChatId();

        return answerMethodFactory.getSendMessage(
                chatId,
                getProfileText(chatId),
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Обновить токен"),
                        List.of(1),
                        List.of(PROFILE_REFRESH_TOKEN)
                )
        );
    }

    private BotApiMethod<?> showProfile(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();

        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                getProfileText(chatId),
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Обновить токен"),
                        List.of(1),
                        List.of(PROFILE_REFRESH_TOKEN)
                )
        );
    }

    private String getProfileText(Long chatId){
        StringBuilder text = new StringBuilder("\uD83D\uDC64 Профиль\n");
        var user = userRepository.findById(chatId).orElseThrow();
        var details = user.getUserDetails();

        if(details.getUsername() != null){
            text.append("▪\uFE0FИмя пользователя - ").append(details.getUsername());
        } else{
            text.append("▪\uFE0FИмя пользователя - ").append(details.getFirstName());
        }
        text.append("\n▪\uFE0FРоль - ").append(user.getRole().name());
        text.append("\n▪\uFE0FВаш уникальный токен* - ").append(user.getToken().toString());
        text.append("\n\n⚠\uFE0F - токен необходим для коммуникации между преподавателем и учеником");
        return text.toString();
    }

    private BotApiMethod<?> refreshToken(CallbackQuery callbackQuery){
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.refreshToken();
        userRepository.save(user);
        return showProfile(callbackQuery);

    }
}
