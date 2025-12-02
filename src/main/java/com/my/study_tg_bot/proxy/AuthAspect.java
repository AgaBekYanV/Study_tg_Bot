package com.my.study_tg_bot.proxy;

import com.my.study_tg_bot.entity.user.Action;
import com.my.study_tg_bot.entity.user.Role;
import com.my.study_tg_bot.entity.user.User;
import com.my.study_tg_bot.repository.UserRepository;
import com.my.study_tg_bot.service.manager.auth.AuthManager;
import com.my.study_tg_bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Aspect
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Order(100)
public class AuthAspect {

    final UserRepository userRepository;
    final AuthManager authManager;

    @Autowired
    public AuthAspect(UserRepository userRepository, AuthManager authManager) {
        this.userRepository = userRepository;
        this.authManager = authManager;
    }

    @Pointcut("execution(* com.my.study_tg_bot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut(){}

    @Around("distributeMethodPointcut()")
    public Object authMethodeAdvice(ProceedingJoinPoint joinPoint) throws Throwable{
        log.info("Я в аспект классе Auth");

        Update update = (Update) joinPoint.getArgs()[0];
        User user;

        if(update.hasMessage()){
            log.info("Передано сообщение");
            user = userRepository.findById(update.getMessage().getChatId()).orElseThrow();
        } else if(update.hasCallbackQuery()){
            log.info("Передан Callback");
            user = userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).orElseThrow();
        } else {
            log.info("Не найдено подходящих сообщения или Callback-а. Продолжаю выполнение без взаимодействия с бд");
            return joinPoint.proceed();
        }

        if(user.getRole() != Role.EMPTY){
            return joinPoint.proceed();
        }

        if(user.getAction() == Action.AUTH){
            return joinPoint.proceed();
        }

        return authManager.answerMessage(
                update.getMessage(),
                (Bot) joinPoint.getArgs()[1]);
    }

}
