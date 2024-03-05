package com.codereview.telegrambotparser.model;

import com.codereview.telegrambotparser.model.base.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_chat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChat extends AbstractEntity {

    @Column(name = "email")
    private String email;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private VacancyType type;

    @Column(name = "grade")
    private String grade;
}
