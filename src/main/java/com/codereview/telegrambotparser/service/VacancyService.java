package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.Vacancy;
import com.codereview.telegrambotparser.model.VacancyType;
import com.codereview.telegrambotparser.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private VacancyRepository repository;

    @Autowired
    public VacancyService(VacancyRepository repository) {
        this.repository = repository;
    }

    public void create(Vacancy vacancyDTO) {
        List<Vacancy> vacancy = repository.findByUrl(vacancyDTO.getUrl());
        if(vacancy.isEmpty()) {
            repository.save(vacancyDTO);
        }
    }

    public List<Vacancy> getAll() {
        return repository.findAll();
    }

    public void addAll(List<Vacancy> vacancies) {
        for(Vacancy vacancy : vacancies) {
            create(vacancy);
        }
    }

    public List<Vacancy> getByTypeAndSite(VacancyType vacancyType, NameSite site) {
        return repository.findByTypeAndSite(vacancyType, site);
    }
}
