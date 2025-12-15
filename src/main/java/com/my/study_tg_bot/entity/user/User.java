package com.my.study_tg_bot.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "token")
    String token;

    @Enumerated(EnumType.STRING)
    @Column(name="role")
    Role role;

    @Enumerated(EnumType.STRING)
    @Column(name="action")
    Action action;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_details_id")
    UserDetails userDetails;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"),
            name = "relationships"
    )
    List<User> users;

    @PrePersist
    private void generateUniqueToken() {
        if(token == null) {
            token = String.valueOf(UUID.randomUUID());
        }
    }

    public void addUser(User user) {
        if(users == null){
            users = new ArrayList<>();
        }
        users.add(user);
    }

    public void refreshToken(){
        token = String.valueOf(UUID.randomUUID());
    }
}
