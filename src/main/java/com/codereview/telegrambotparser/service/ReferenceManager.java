package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.VacancyType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceManager {
    private final String HH = "Head Hunter";
    private final String HABR = "Хабр Карьера";
    private final String GEEK_JOB = "Geekjob";
    private final String HEKSLET = "Хекслет CV";
    private final String JOBBY = "Jobby";
    private final String JAVA = "Java";
    private final String PYTHON = "Python";
    private final String JAVASCRIPT = "Java Script";
    private final String DATASCIENCE = "Data Science";
    private final String QA = "QA";
    private final String CSHARP = "C#";
    static Map<String, String> refSelect;

    @PostConstruct
    void initialize() {
        log.info("init loading");
        List<String> nameSiteList = addNameList();
        refSelect = new HashMap<>();
        for(NameSite site : NameSite.values()) {
            refSelect.put(site.name(), nameSiteList.get(site.ordinal()));
        }
        List<String> vacancyTypeList = addTypeList();
        for(VacancyType type : VacancyType.values()) {
            refSelect.put(type.name(), vacancyTypeList.get(type.ordinal()));
        }
   }

    public String getNameReference(String nameRef) {
        String name = refSelect.get(nameRef);
        return name.isEmpty() ? "this name name don't know" : name;
    }

    private List<String> addNameList() {
        List<String> nameList = new ArrayList<>();
        nameList.add(HH);
        nameList.add(HABR);
        nameList.add(GEEK_JOB);
        nameList.add(HEKSLET);
        nameList.add(JOBBY);
        return nameList;
    }

    private List<String> addTypeList() {
        List<String> typeList = new ArrayList<>();
        typeList.add(JAVA);
        typeList.add(PYTHON);
        typeList.add(JAVASCRIPT);
        typeList.add(DATASCIENCE);
        typeList.add(QA);
        typeList.add(CSHARP);
        return typeList;
    }
}
