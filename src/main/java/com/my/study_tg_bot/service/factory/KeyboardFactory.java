package com.my.study_tg_bot.service.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KeyboardFactory {

    public InlineKeyboardMarkup getInlineKeyboardMarkup(
            List<String> text,
            List<Integer> configuration,
            List<String> data
    ) {
        log.info("Формирую клавиатуру InlineKeyboardMarkup");
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        int index = 0;
        for (Integer rowNumber : configuration) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = 0; i < rowNumber; i++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(text.get(index));
                button.setCallbackData(data.get(index));
                row.add(button);
                index++;
            }
            keyboard.add(row);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getReplyKeyboardMarkup(
           List<String> text,
           List<Integer> configuration
    ) {
        log.info("Формирую клавиатуру ReplyKeyboardMarkup");
        List<KeyboardRow> keyboard = new ArrayList<>();
        int index = 0;
        for (Integer rowNumber : configuration) {
            KeyboardRow keyboardRow = new KeyboardRow();
            for (int i = 0; i < rowNumber; i++) {
                KeyboardButton button = new KeyboardButton();
                button.setText(text.get(index));
                keyboardRow.add(button);
                index++;
            }
            keyboard.add(keyboardRow);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
}
