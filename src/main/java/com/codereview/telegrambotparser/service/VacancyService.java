package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.Vacancy;
import com.codereview.telegrambotparser.model.VacancyType;
import com.codereview.telegrambotparser.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyService {

    private VacancyRepository repository;

    @Autowired
    public VacancyService(VacancyRepository repository) {
        this.repository = repository;
    }

    public void create(Vacancy vacancyDTO) {
        log.info("create vacation {}", vacancyDTO);
        List<Vacancy> vacancy = repository.findByUrl(vacancyDTO.getUrl());
        if(vacancy.isEmpty()) {
            repository.save(vacancyDTO);
        }
    }

    public List<Vacancy> getAll() {
        log.info("get all vacancies");
        return repository.findAll();
    }

    public void addAll(List<Vacancy> vacancies) {
        for(Vacancy vacancy : vacancies) {
            create(vacancy);
        }
    }

    public List<Vacancy> getByTypeAndSite(VacancyType vacancyType, NameSite site) {
        log.info("get vacancies by type {} and sate {}", vacancyType, site);
        return repository.findByTypeAndSite(vacancyType, site);
    }

    public List<Vacancy> getByTypeAndSiteForLastHour(VacancyType vacancyType, NameSite site) {
        log.info("get vacancies by type {} site {} last hour", vacancyType, site);
        return repository.findByTypeAndSiteForLastHour(vacancyType, site, LocalDateTime.now().minusHours(1));
    }
}
