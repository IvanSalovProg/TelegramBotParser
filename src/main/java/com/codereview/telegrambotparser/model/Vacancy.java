package com.codereview.telegrambotparser.model;

import lombok.Data;

@Data
public class Vacancy {
    private int position;
    private String name;
    private String company;
    private String Description;
    private String url;
}
