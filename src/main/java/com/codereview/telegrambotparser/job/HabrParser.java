package com.codereview.telegrambotparser.job;

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
    private final String Habr_URL;
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public HabrParser(String vacancyType) {
        Habr_URL = URL_PART1 + vacancyType.toLowerCase() + URL_PART2;
        this.vacancyType = VacancyType.valueOf(vacancyType.toUpperCase());
    }

    public List<Vacancy> start() {
        return parser();
    }

    public String nextPageUrl(int page) {
        return Habr_URL + page;
    }



    @Override
    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 20) return new ArrayList<>();

        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass("vacancy-card");

        List<Vacancy> element = elements.stream().map(q -> {
            Vacancy vacancyInformation = new Vacancy();
            Element titleElement1 = q.getElementsByClass("vacancy-card__title-link").first();
            if (titleElement1 != null) {
                String element1 = "";
                element1 = titleElement1.getElementsByTag("a").text();
                Element titleElement2 = q.getElementsByClass("vacancy-card__company").first();
                String element2 = "";
                if (titleElement2 != null)
                    element2 = titleElement2.getElementsByTag("a").text();
                Element titleElement3 = q.getElementsByClass("vacancy-card__info").first();
                String element3 = "";
                if (titleElement3 != null)
                    element3 = titleElement3.getElementsByClass("vacancy-card__skills").text();

                vacancyInformation.setLocation(element1);
                vacancyInformation.setName(element2);
                vacancyInformation.setCompany(element3);
               // vacancyInformation.setUrl(url);
              //  vacancyInformation.setGrade(grade);
               // vacancyInformation.setSchedule(schedule);
                vacancyInformation.setType(vacancyType);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();

       // element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }
}
