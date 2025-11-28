package com.my.study_tg_bot.proxy;


import com.my.study_tg_bot.entity.user.Action;
import com.my.study_tg_bot.entity.user.Role;
import com.my.study_tg_bot.entity.user.UserDetails;
import com.my.study_tg_bot.repository.UserDetailsRepository;
import com.my.study_tg_bot.repository.UserRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDate;

@Aspect
@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationAspect {

    final UserRepository userRepository;
    final UserDetailsRepository userDetailsRepository;

    @Autowired
    public UserCreationAspect(UserRepository userRepository,
                              UserDetailsRepository userDetailsRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    @Pointcut("execution(* com.my.study_tg_bot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut(){}

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable{
        log.info("Я в аспект классе");
        Object[] args = joinPoint.getArgs();
        Update update = (Update) args[0];
        User telegramUser;
        if(update.hasMessage()){
            log.info("Передано сообщение");
            telegramUser = update.getMessage().getFrom();
        } else if(update.hasCallbackQuery()){
            log.info("Передан Callback");
            telegramUser = update.getCallbackQuery().getFrom();
        } else {
            log.info("Не найдено подходящих сообщения или Callback-а. Продолжаю выполнение без взаимодействия с бд");
            return joinPoint.proceed();
        }



        if(userRepository.existsById(telegramUser.getId())){
            log.info("Пользователь с таким chatId уже существует в бд");
            return joinPoint.proceed();
        }

        log.info("Записываю детали");
        UserDetails userDetails = UserDetails.builder()
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .username(telegramUser.getUserName())
                .registeredAt(LocalDate.now())
                .build();

        log.info("Сохраняю детали");
        userDetailsRepository.save(userDetails);



        log.info("Записываю пользователя");
        com.my.study_tg_bot.entity.user.User newUser = com.my.study_tg_bot.entity.user.User.builder()
                .chatId(telegramUser.getId())
                .action(Action.FREE)
                .role(Role.USER)
                .userDetails(userDetails)
                .build();

        //userDetails.setUser(newUser);

        log.info("Сохраняю пользователя");
        userRepository.save(newUser);


        return joinPoint.proceed();
    }
}
