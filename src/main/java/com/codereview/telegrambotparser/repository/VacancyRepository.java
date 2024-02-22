package com.codereview.telegrambotparser.repository;

import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.Vacancy;
import com.codereview.telegrambotparser.model.VacancyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    @Query("SELECT v FROM Vacancy v WHERE v.url =:url")
    List<Vacancy> findByUrl(String url);

    @Query("SELECT v FROM Vacancy v WHERE v.type =:type AND v.site =:site")
    List<Vacancy> findByTypeAndSite(VacancyType type, NameSite site);

    @Query("SELECT v FROM Vacancy v WHERE v.type =:type AND v.site =:site AND v.dateTime >:dateTime")
    List<Vacancy> findByTypeAndSiteForLastHour(VacancyType type, NameSite site, LocalDateTime dateTime);
}
