package com.my.study_tg_bot.repository;

import com.my.study_tg_bot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByChatId(Long chatId);

    User findUserByToken(String token);
}
