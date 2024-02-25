package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.model.NameSite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    static Map<NameSite, String> refSiteSelect;

    @PostConstruct
    void initialize() {
        log.info("init loading");
        List<String> nameSiteList = addNameList();
        refSiteSelect = new HashMap<>();
        for(NameSite site : NameSite.values()) {
            refSiteSelect.put(site, nameSiteList.get(site.ordinal()));
        }
   }

    public String getNameSite(NameSite nameSite) {
        String site = refSiteSelect.get(nameSite);
        return site.isEmpty() ? "this name site don't know" : site;
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
}
