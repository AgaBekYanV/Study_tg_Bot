package com.my.study_tg_bot.service.handler;

import com.my.study_tg_bot.telegram.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


@Service
public class CallbackQueryHandler {
    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot){
        return null;
    }
}
