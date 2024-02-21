package com.codereview.telegrambotparser.repository;

import com.codereview.telegrambotparser.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    @Query("SELECT v FROM Vacancy v WHERE v.url =:url")
    List<Vacancy> findByUrl(String url);

}
