package com.my.study_tg_bot.service.manager.timetable;

import com.my.study_tg_bot.entity.timetable.Timetable;
import com.my.study_tg_bot.entity.timetable.WeekDay;
import com.my.study_tg_bot.entity.user.Role;
import com.my.study_tg_bot.entity.user.User;
import com.my.study_tg_bot.repository.TimetableRepository;
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
import java.util.UUID;

import static com.my.study_tg_bot.service.data.CallbackData.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;
    final TimetableRepository timetableRepository;

    @Autowired
    public TimetableManager(AnswerMethodFactory answerMethodFactory,
                            KeyboardFactory keyboardFactory,
                            UserRepository userRepository,
                            TimetableRepository timetableRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
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
        String text = callbackQuery.getData();
        String[] splitCallbackData = text.split("_");
        if(splitCallbackData.length > 1 && "add".equals(splitCallbackData[1])) {
            if(splitCallbackData.length == 2 || splitCallbackData.length == 3) {
                return add(callbackQuery,splitCallbackData);

            }
            switch (splitCallbackData[2]) {
                case WEEKDAY -> {
                    return addWeekDay(callbackQuery, splitCallbackData);
                }
                case HOUR -> {
                    return addHour(callbackQuery, splitCallbackData);
                }
                case MINUTE -> {
                    return addMinute(callbackQuery, splitCallbackData);
                }
                case USER -> {
                    return addUser(callbackQuery, splitCallbackData);
                }
            }
        }
        switch (text) {
            case TIMETABLE -> { return mainMenu(callbackQuery);}
            case TIMETABLE_SHOW -> {return show(callbackQuery);}
            case TIMETABLE_REMOVE -> {return remove(callbackQuery);}
            case TIMETABLE_1, TIMETABLE_2, TIMETABLE_3,
                 TIMETABLE_4,TIMETABLE_5,TIMETABLE_6,
                 TIMETABLE_7 -> {
                return showDay(callbackQuery);
            }

        }
        return null;
    }




    private BotApiMethod<?> mainMenu(Message message){
        var user = userRepository.findUserByChatId(message.getChatId());
        if(user.getRole() == Role.STUDENT){
            return answerMethodFactory.getSendMessage(
                    message.getChatId(),
                    """
                            📆 Здесь вы можете посмотреть ваше расписание""",
                    keyboardFactory.getInlineKeyboardMarkup(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        }
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Показать мое расписание",
                                "Удалить занятие", "Добавить занятие"),
                        List.of(1,2),
                        List.of(TIMETABLE_SHOW,TIMETABLE_REMOVE,TIMETABLE_ADD)
                )
        );

    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        if(user.getRole() == Role.STUDENT){
            return answerMethodFactory.getEditMessageText(
                    callbackQuery,
                    """
                           📆 Здесь вы можете посмотреть ваше расписание""",
                    keyboardFactory.getInlineKeyboardMarkup(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        }
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Показать мое расписание",
                                "Удалить занятие", "Добавить занятие"),
                        List.of(1,2),
                        List.of(TIMETABLE_SHOW,TIMETABLE_REMOVE,TIMETABLE_ADD)
                )
        );
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Выберете день недели""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of( "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
                                "Назад"),
                        List.of(4,3,1),
                        List.of(TIMETABLE_1, TIMETABLE_2, TIMETABLE_3, TIMETABLE_4,
                                TIMETABLE_5, TIMETABLE_6, TIMETABLE_7,
                                TIMETABLE)
                )
        );
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery, String[] splitCallbackData){
        String id;
        if(splitCallbackData.length == 2){
            var timetable = new Timetable();
            timetable.addUser(userRepository.findUserByChatId(callbackQuery.getMessage().getChatId()));
            id = timetableRepository.save(timetable).getId().toString();
        } else {
            id = splitCallbackData[2];
        }
        List<String> data = new ArrayList<>();
        for(int i = 1; i <= 7; i++){
            data.add(TIMETABLE_ADD_WEEKDAY + i + "_" + id);
        }
        data.add(TIMETABLE);
        return answerMethodFactory.getEditMessageText(
            callbackQuery,
            """
                   ✏️ Выберете день, в который хотите добавить занятие:""",
            keyboardFactory.getInlineKeyboardMarkup(
                    List.of("Понедельник", "Вторник", "Среда", "Четверг",
                            "Пятница", "Суббота", "Воскресенье",
                            "\uD83D\uDD19Назад"),
                    List.of(4,3,1),
                    data
            )
    );}

    private BotApiMethod<?> remove(CallbackQuery callbackQuery){return answerMethodFactory.getEditMessageText(
            callbackQuery,
            """
                    ✂️ Выберете занятие, которое хотите удалить из вашего расписания""",
            keyboardFactory.getInlineKeyboardMarkup(
                    List.of("\uD83D\uDD19Назад"),
                    List.of(1),
                    List.of(TIMETABLE)
            )
    );}

    private BotApiMethod<?> showDay(CallbackQuery callbackQuery) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        WeekDay weekDay = WeekDay.MONDAY;
        switch (callbackQuery.getData().split("_")[1]) {
            case "2" -> {weekDay = WeekDay.TUESDAY;}
            case "3" -> {weekDay = WeekDay.WEDNESDAY;}
            case "4" -> {weekDay = WeekDay.THURSDAY;}
            case "5" -> {weekDay = WeekDay.FRIDAY;}
            case "6" -> {weekDay = WeekDay.SATURDAY;}
            case "7" -> {weekDay = WeekDay.SUNDAY;}
        }
        List<Timetable> timetableList = timetableRepository.findAllByUsersContainingAndWeekDay(user, weekDay);
        StringBuilder text = new StringBuilder();
        if(timetableList == null || timetableList.isEmpty()){
            text.append("У вас нет занятий в этот день.");
        } else {
            text.append("Ваши занятия на сегодня:\n\n");
            for(Timetable t: timetableList){
                text.append("▪\uFE0F ")
                        .append(t.getHour())
                        .append(":")
                        .append(t.getMinute())
                        .append(" - ")
                        .append(t.getTitle())
                        .append("\n\n");


            }
        }
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)
                )
        );
    }

    private BotApiMethod<?> addWeekDay(CallbackQuery callbackQuery, String[] data) {
        UUID id = UUID.fromString(data[4]);
        var timetable = timetableRepository.findTimetableById(id);
        switch (data[3]) {
            case "1" -> {
                timetable.setWeekDay(WeekDay.MONDAY);
            }
            case "2" -> {
                timetable.setWeekDay(WeekDay.TUESDAY);
            }
            case "3" -> {
                timetable.setWeekDay(WeekDay.WEDNESDAY);
            }
            case "4" -> {
                timetable.setWeekDay(WeekDay.THURSDAY);
            }
            case "5" -> {
                timetable.setWeekDay(WeekDay.FRIDAY);
            }
            case "6" -> {
                timetable.setWeekDay(WeekDay.SATURDAY);
            }
            case "7" -> {
                timetable.setWeekDay(WeekDay.SUNDAY);
            }
        }
        List<String> buttonsData = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            text.add(String.valueOf(i));
            buttonsData.add(TIMETABLE_ADD_HOUR + i + "_" + data[4]);
        }
        buttonsData.add(TIMETABLE_ADD_WEEKDAY + "_" + data[4]);
        text.add("Назад");
        timetableRepository.save(timetable);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Выберите час",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        List.of(6, 6, 6, 6, 1),
                        buttonsData
                )
        );
    }

    private BotApiMethod<?> addHour(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timetable = timetableRepository.findTimetableById(UUID.fromString(id));
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        timetable.setHour(Short.valueOf(splitCallbackData[3]));
        int min = 0;
        for (int i = 1; i <=11 ; i++) {
            if(i == 1 || i == 2){
                String minText = "0" + i;
                text.add(minText);
                data.add(TIMETABLE_ADD_MINUTE + minText + "_" + id);
            } else {
                text.add(String.valueOf(min));
                data.add(TIMETABLE_ADD_MINUTE + min + "_" + id);

            }
            min+=5;

        }
        text.add("Назад");

        switch (timetable.getWeekDay()) {
            case MONDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 1 + "_" + id);
            case TUESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 2 + "_" + id);
            case WEDNESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 3 + "_" + id);
            case THURSDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 4 + "_" + id);
            case FRIDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 5 + "_" + id);
            case SATURDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 6 + "_" + id);
            case SUNDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 7 + "_" + id);
        }
        timetableRepository.save(timetable);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Выерите минуту",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        List.of(4,4,3,1),
                        data
                )
        );
    }

    private BotApiMethod<?> addMinute(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timetable = timetableRepository.findTimetableById(UUID.fromString(id));
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        timetable.setMinute(Short.valueOf(splitCallbackData[3]));
        int index = 0;
        for(User user : userRepository.
                findAllByUsersContaining(
                        userRepository.findUserByChatId(callbackQuery.getMessage().getChatId() )
                )){
            text.add(user.getUserDetails().getUsername());
            data.add(TIMETABLE_ADD_USER + user.getChatId() + "_" + id);
            if(index == 5){
                cfg.add(5);
                index = 0;
            } else {
                index++;
            }
        }
        cfg.add(index);
        cfg.add(1);
        data.add(TIMETABLE_ADD_HOUR + timetable.getHour() + "_" + id);
        text.add("Назад");
        timetableRepository.save(timetable);

        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Выберите ученика",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                )
        );
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        var timetable = timetableRepository.findTimetableById(UUID.fromString(splitCallbackData[4]));
        var user = userRepository.findUserByChatId(Long.valueOf(splitCallbackData[3]));
        timetable.addUser(user);
        timetable.setTitle(user.getUserDetails().getFirstName());
        timetableRepository.save(timetable);

        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Успешно",
                null
        );
    }
}
