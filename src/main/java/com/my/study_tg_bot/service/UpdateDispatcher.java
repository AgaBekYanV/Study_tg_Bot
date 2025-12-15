package com.my.study_tg_bot.service;

import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.handler.CallbackQueryHandler;
import com.my.study_tg_bot.service.handler.CommandHandler;
import com.my.study_tg_bot.service.handler.MessageHandler;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateDispatcher {

    final MessageHandler messageHandler;
    final CommandHandler commandHandler;
    final CallbackQueryHandler callbackQueryHandler;

    @Autowired
    public UpdateDispatcher(
            MessageHandler messageHandler,
            CommandHandler commandHandler,
            CallbackQueryHandler callbackQueryHandler,
            UserRepository userRepository
    ) {
        this.messageHandler = messageHandler;
        this.commandHandler = commandHandler;
        this.callbackQueryHandler = callbackQueryHandler;
    }

    public BotApiMethod <?> distribute(Update update, Bot bot) {
        log.info("Распределение сообщений. UpdateDispatcher");
        if(update.hasCallbackQuery()){
            return callbackQueryHandler.answer(update.getCallbackQuery(),bot);
        }
        if(update.hasMessage()){
            log.info("Распознал сообщение. UpdateDispatcher");
            Message message = update.getMessage();
            if(message.hasText()) {
                log.info("Распознал текст. UpdateDispatcher");
                if (message.getText().charAt(0) == '/') {
                    log.info("Распознал команду. UpdateDispatcher");
                    log.info("Обработка команды. UpdateDispatcher");
                    return commandHandler.answer(message, bot);
                }
            }
            log.info("Обработка сообщения. UpdateDispatcher");
            return messageHandler.answer(message, bot);
        }
        log.info("Unsupported update {}", update);
        return null;

    }
}
