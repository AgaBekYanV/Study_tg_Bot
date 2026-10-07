package com.my.study_tg_bot.service.manager.timetable;

import com.my.study_tg_bot.entity.timetable.Timetable;
import com.my.study_tg_bot.entity.timetable.WeekDay;
import com.my.study_tg_bot.entity.user.Action;
import com.my.study_tg_bot.entity.user.Role;
import com.my.study_tg_bot.entity.user.User;
import com.my.study_tg_bot.repository.TimetableRepository;
import com.my.study_tg_bot.repository.UserDetailsRepository;
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
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.my.study_tg_bot.service.data.CallbackData.*;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;
    final UserDetailsRepository userDetailsRepository;
    final TimetableRepository timetableRepository;


    @Autowired
    public TimetableManager(AnswerMethodFactory answerMethodFactory,
                            KeyboardFactory keyboardFactory,
                            UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            TimetableRepository timetableRepository
    ) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.timetableRepository = timetableRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        var user = userRepository.findUserByChatId(message.getChatId());
        try {
            bot.execute(answerMethodFactory.getDeleteMessageText(
                    message.getChatId(),
                    message.getMessageId() -1
            ));
            bot.execute(answerMethodFactory.getSendMessage(
                    message.getChatId(),
                    "Значение успешно установлено",
                    null
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        switch (user.getAction()){
            case SENDING_TITLE -> {return setTitle(message, user);}
            case SENDING_DESCRIPTION -> {return setDescription(message, user);}
        }
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
                case TITLE -> {
                    return askTitle(callbackQuery, splitCallbackData);
                }
                case DESCRIPTION -> {
                    return askDescription(callbackQuery, splitCallbackData);
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
        if(FINISH.equals(splitCallbackData[1])){
            try {
                return finish(callbackQuery, splitCallbackData, bot);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }if(BACK.equals(splitCallbackData[1])){

            return back(callbackQuery, splitCallbackData);

        }
        if(splitCallbackData.length > 2 && REMOVE.equals(splitCallbackData[1])){
            switch (splitCallbackData[2]) {
                case WEEKDAY -> {return removeWeekday(callbackQuery, splitCallbackData[3]);}
                case POS -> {return askConfirmation(callbackQuery, splitCallbackData);}
                case FINAL -> {
                    try {
                        return deleteTimetable(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> deleteTimetable(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        var timeTable = timetableRepository.findTimetableById(UUID.fromString(id));
        timeTable.setUsers(null);
        timetableRepository.delete(timeTable);
        bot.execute(
                answerMethodFactory.getAnswerCallbackQuery(
                        callbackQuery.getId(),
                        "Запись \" " + timeTable.getTitle() + "\" успешно удалена!"
                )
        );

        return answerMethodFactory.getDeleteMessageText(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String[] splitCallbackData) {
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        Вы уверены, что хотите удалить запись из расписания?""",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Да", "Нет"),
                        List.of(2),
                        List.of(TIMETABLE_REMOVE_FINAL + splitCallbackData[3],
                                TIMETABLE_REMOVE_WEEKDAY + splitCallbackData[4])
                )
        );
    }

    private BotApiMethod<?> removeWeekday(CallbackQuery callbackQuery, String number) {
        WeekDay weekDay = WeekDay.MONDAY;
        switch (number){
            case "2" -> weekDay = WeekDay.TUESDAY;
            case "3" -> weekDay = WeekDay.WEDNESDAY;
            case "4" -> weekDay = WeekDay.THURSDAY;
            case "5" -> weekDay = WeekDay.FRIDAY;
            case "6" -> weekDay = WeekDay.SATURDAY;
            case "7" -> weekDay = WeekDay.SUNDAY;
        }
        List <String> data = new ArrayList<>();
        List <String> text = new ArrayList<>();
        List <Integer> cfg = new ArrayList<>();
        for(Timetable timetable: timetableRepository.findAllByUsersContainingAndWeekDay(
                userRepository.findUserByChatId(callbackQuery.getMessage().getChatId()),
                weekDay
        )){
            data.add(TIMETABLE_REMOVE_POS + timetable.getId() + "_" + number);
            text.add(timetable.getTitle() + " " + timetable.getHour() + ":" + timetable.getMinute() +
                    " " + userRepository.findUserByChatId(callbackQuery.getMessage().getChatId()));
        }

        cfg.add(1);
        data.add(TIMETABLE_REMOVE);
        text.add("Назад");
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        Выберите занятие которое хотите убрать из расписания""",
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                )
        );
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
                        List.of( "Пн", "Вт", "Ср", "Чт",
                                "Пт", "Сб", "Вс",
                                "Назад"),
                        List.of(7,1),
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
            timetable.setInCreation(true);
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
                    List.of("Пн", "Вт", "Ср", "Чт",
                            "Пт", "Сб", "Вс",
                            "\uD83D\uDD19Назад"),
                    List.of(7,1),
                    data
            )
    );}

    private BotApiMethod<?> remove(CallbackQuery callbackQuery){
        List<String> data = new ArrayList<>();

        for(int i = 1; i <= 7; i++){
            data.add(TIMETABLE_REMOVE_WEEKDAY + i);
        }
        data.add(TIMETABLE);
        return answerMethodFactory.getEditMessageText(
            callbackQuery,
            """
                    ✂️ Выберете день""",
            keyboardFactory.getInlineKeyboardMarkup(
                    List.of("Пн", "Вт", "Ср", "Чт",
                            "Пт", "Сб", "Вс","\uD83D\uDD19Назад"),
                    List.of(7, 1),
                    data
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
        for (int i = 1; i <=12 ; i++) {
            if(i == 1 || i == 2){
                String minText = "0" + min;
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
                        List.of(4,4,4,1),
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
        var teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        for(User user : teacher.getUsers()){
            text.add(user.getUserDetails().getFirstName());
            data.add(TIMETABLE_ADD_USER + user.getChatId() + "_" + id);
            if(index == 5){
                cfg.add(5);
                index = 0;
            } else {
                index++;
            }
        }
        if(index != 0){
            cfg.add(index);
        }
        cfg.add(1);
        data.add(TIMETABLE_ADD_HOUR + timetable.getHour() + "_" + id);
        text.add("Назад");
        timetableRepository.save(timetable);

        String messageText = "Выберите ученика";
        if(cfg.size() == 1){
            messageText = "У вас нет ни одного ученика";
        }

        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                messageText,
                keyboardFactory.getInlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                )
        );
    }

    private BotApiMethod<?> back(Message message, String timetableId){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Изменить заголовок", "Изменить описание", "Завершить создание"),
                        List.of(2,1),
                        List.of(TIMETABLE_ADD_TITLE + timetableId,
                                TIMETABLE_ADD_DESCRIPTION + timetableId,
                                TIMETABLE_FINISH + timetableId)
                )
        );
    }
    private BotApiMethod<?> back(CallbackQuery callbackQuery, String[] splitCallbackData){
        String id = splitCallbackData[2];
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.FREE);
        userRepository.save(user);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Изменить заголовок", "Изменить описание", "Завершить создание"),
                        List.of(2,1),
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id)
                )
        );
    }


    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timetable = timetableRepository.findTimetableById(UUID.fromString(id));
        var user = userRepository.findUserByChatId(Long.valueOf(splitCallbackData[3]));


        timetable.addUser(user);
        timetable.setTitle(user.getUserDetails().getFirstName());
        timetableRepository.save(timetable);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Успешно запись добавлена, теперь вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id),
                        List.of(2,1),
                        List.of("Изменить заголовок", "Изменить описание", "Завершить создание")
                )
        );
    }

    private BotApiMethod<?> finish(CallbackQuery callbackQuery, String[] splitCallbackData, Bot bot) throws TelegramApiException {
        var timetable = timetableRepository.findTimetableById(UUID.fromString(splitCallbackData[2]));
        timetable.setInCreation(false);
        timetableRepository.save(timetable);
        bot.execute(answerMethodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(),
                "Процесс создания записи в расписании успешно завершен"

        ));

        return answerMethodFactory.getDeleteMessageText(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> askTitle(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[3];
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_TITLE);
        var details = user.getUserDetails();
        details.setTimetableId(id);
        userDetailsRepository.save(details);
        user.setUserDetails(details);
        userRepository.save(user);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Введите заголовок: ",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK + id)
                )
        );
    }

    private BotApiMethod<?> askDescription(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[3];
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_DESCRIPTION);
        var details = user.getUserDetails();
        details.setTimetableId(id);
        userDetailsRepository.save(details);
        user.setUserDetails(details);
        userRepository.save(user);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Введите описание: ",
                keyboardFactory.getInlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK + id)
                )
        );
    }

    private BotApiMethod<?> setTitle(Message message, User user) {
        user.setAction(Action.FREE);
        userRepository.save(user);
        String id = user.getUserDetails().getTimetableId();
        var timetable = timetableRepository.findTimetableById(
                UUID.fromString(id)
        );
        timetable.setTitle(message.getText());
        timetableRepository.save(timetable);
        return back(message,id);
    }

    private BotApiMethod<?> setDescription(Message message, User user) {
        user.setAction(Action.FREE);
        userRepository.save(user);
        String id = user.getUserDetails().getTimetableId();
        var timetable = timetableRepository.findTimetableById(
                UUID.fromString(id)
        );
        timetable.setDescription(message.getText());
        timetableRepository.save(timetable);
        return back(message,id);
    }

}
