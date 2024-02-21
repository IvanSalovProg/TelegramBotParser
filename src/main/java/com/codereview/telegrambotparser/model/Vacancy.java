package com.codereview.telegrambotparser.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "vacancy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "company")
    private String company;

    @Column(name = "location")
    private String location;

    @Column(name = "schedule")
    private String schedule;

    @Column(name = "grade")
    private String grade;

    @Column(name = "type")
    private VacancyType type;

    @Column(name = "url")
    private String url;
}
