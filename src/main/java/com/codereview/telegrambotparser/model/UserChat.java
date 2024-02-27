package com.codereview.telegrambotparser.model;

import com.codereview.telegrambotparser.model.base.AbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;

@Entity
@Table(name = "user_chat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChat extends AbstractEntity {

/*    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;*/

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "type")
    private VacancyType type;

    @Column(name = "grade")
    private String grade;
}
