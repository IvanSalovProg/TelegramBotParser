package com.codereview.telegrambotparser.job;

import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.Vacancy;
import com.codereview.telegrambotparser.model.VacancyType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HabrParser extends VacancyParser {  //https://career.habr.com/vacancies?q=C%23&s%5B%5D=2&type=all
    //https://career.habr.com/vacancies?q=C%23&s[]=2&type=all
    private final String URL_PART1 = "https://career.habr.com/vacancies?q=";
    private final String URL_PART2 = "&type=all";
    private final String C_SHARP = "C%23&s%5B%5D=2";
    private final String JAVA_ = "java&s[]=2";
    private final String DATA_SCIENCE = "DATA%20SCIENCE&s[]=44";
    private final String QA_ = "QA";
    private final String JAVASCRIPT_ = "JAVASCRIPT&s[]=3";
    private final String PYTHON_ = "PYTHON&s[]=2";

    private final String Habr_URL;
    private final String PARS_VACANCIES = "vacancy-card";
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public HabrParser(VacancyType vacancyType) {
        if(vacancyType.equals(VacancyType.CSHARP)) {
            Habr_URL = URL_PART1 + C_SHARP + URL_PART2;
        } else if(vacancyType.equals(VacancyType.DATASCIENCE)) {
            Habr_URL = URL_PART1 + DATA_SCIENCE + URL_PART2;
        } else if(vacancyType.equals(VacancyType.JAVA)) {
            Habr_URL = URL_PART1 + JAVA_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.QA)) {
            Habr_URL = URL_PART1 + QA_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.JAVASCRIPT)) {
            Habr_URL = URL_PART1 + JAVASCRIPT_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.PYTHON)) {
            Habr_URL = URL_PART1 + PYTHON_ + URL_PART2;
        }
        else {Habr_URL = URL_PART1 + vacancyType.name().toLowerCase() + URL_PART2;}
        
        this.vacancyType = vacancyType;
      /*   String vacancy = "";
        VacancyType type = null;
        if(vacancyType.equals(VacancyType.JAVA)) {
            vacancy = JAVA_;
            type = VacancyType.JAVA;
        }
        if(vacancyType.equals(VacancyType.CSHARP)) {
            vacancy = C_SHARP;
            type = VacancyType.CSHARP;
        }
        if(vacancyType.equals(VacancyType.PYTHON)) {
            vacancy = PYTHON_;
            type = VacancyType.CSHARP;
        }
        if(vacancyType.equals(VacancyType.JAVASCRIPT)) {
            vacancy = JAVASCRIPT_;
            type = VacancyType.CSHARP;
        }
        if(vacancyType.equals(VacancyType.DATASCIENCE)) {
            vacancy = DATA_SCIENCE;
            type = VacancyType.CSHARP;
        }
        if(vacancyType.equals(VacancyType.QA)) {
            vacancy = QA_;
            type = VacancyType.CSHARP;
        }
        Habr_URL = URL_PART1 + vacancy + URL_PART2;
        this.vacancyType = type; */
    }

    public List<Vacancy> start() {
        return parser();
    }

    public String nextPageUrl(int page) {
        return Habr_URL + page;
    }

    @Override
    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 10) return new ArrayList<>();

        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass(PARS_VACANCIES);

        List<Vacancy> element = elements.stream().map(q -> {
            Vacancy vacancyInformation = new Vacancy();
            Element titleName = q.getElementsByClass("vacancy-card__title-link").first();
            if (titleName != null) {
                String name = "";
                name = titleName.getElementsByTag("a").text();
                Element titleCompany = q.getElementsByClass("vacancy-card__company-title").first();
                String company = "";
                if (titleCompany != null)
                    company = titleCompany.getElementsByTag("a").text();
                Element titleLocation = q.getElementsByClass("vacancy-card__meta").first();
                String location = "";
                if (titleLocation != null)
                    location = titleLocation.getElementsByClass("vacancy-card__meta").text();
                Element titleSchedule = q.getElementsByClass("vacancy-card__meta").first();
                String schedule = "";
                if (titleSchedule != null)
                    schedule = titleSchedule.getElementsByClass("inline-list").text();
                Element titleGrade = q.getElementsByClass("inline-list").first();
                String grade = "";
                if (titleGrade != null)
                    grade = titleGrade.getElementsByClass("vacancy-card__skills").text();

                String url = "https://career.habr.com" + titleName.attr("href");

                vacancyInformation.setLocation(location);
                vacancyInformation.setName(name);
                vacancyInformation.setCompany(company);
                vacancyInformation.setUrl(url);
                vacancyInformation.setGrade(grade);
                vacancyInformation.setSchedule(schedule);
                vacancyInformation.setType(vacancyType);
                vacancyInformation.setSite(NameSite.HABR);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();

       // element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }
}
