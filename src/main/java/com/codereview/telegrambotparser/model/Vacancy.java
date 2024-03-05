package com.codereview.telegrambotparser.model;

import com.codereview.telegrambotparser.model.base.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy extends AbstractEntity {

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

    @Column(name = "site")
    private NameSite site;

    @Column(name = "type")
    private VacancyType type;

    @Column(name = "url")
    private String url;

    @UpdateTimestamp
    @Column(name = "date_time")
    private LocalDateTime dateTime;
}
