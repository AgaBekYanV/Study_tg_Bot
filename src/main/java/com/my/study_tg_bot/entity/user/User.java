package com.my.study_tg_bot.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "id")
    Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name="role")
    Role role;

    @Enumerated(EnumType.STRING)
    @Column(name="action")
    Action action;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_details_id")
    UserDetails userDetails;
}
