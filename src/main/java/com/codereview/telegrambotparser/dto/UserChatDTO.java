package com.codereview.telegrambotparser.dto;


import com.codereview.telegrambotparser.model.VacancyType;
import com.codereview.telegrambotparser.model.base.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserChatDTO implements Identifiable {

    private Long id;

    private Long chatId;

    private VacancyType type;

    private String grade;

    private Long wages;
}
