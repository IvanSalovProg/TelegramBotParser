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
    private final String Habr_URL;
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public HabrParser(String vacancyType) {
        String vacancy = "";
        VacancyType type = null;
        if(vacancyType.equals("Java")) {
            vacancy = "java&s[]=2";
            type = VacancyType.JAVA;
        }
        if(vacancyType.equals("C#")) {
            vacancy = "C%23&s%5B%5D=2";
            type = VacancyType.C_SHARP;
        }
        //HabrParser habrParserCharp = new HabrParser("C%23&s%5B%5D=2");
        //HabrParser habrParserJava = new HabrParser("java&s[]=2");
        Habr_URL = URL_PART1 + vacancy + URL_PART2;
        this.vacancyType = type;
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
            Element titleName = q.getElementsByClass("vacancy-card__title-link").first();
            if (titleName != null) {
                String name = "";
                name = titleName.getElementsByTag("a").text();
                Element titleCompany = q.getElementsByClass("vacancy-card__company").first();
                String company = "";
                if (titleCompany != null)
                    company = titleCompany.getElementsByTag("a").text();
                Element titleLocation = q.getElementsByClass("vacancy-card__meta").first();
                String location = "";
                if (titleLocation != null)
                    location = titleLocation.getElementsByClass("inline-list").text();
                Element titleSchedule = q.getElementsByClass("vacancy-card__meta").first();
                String schedule = "";
                if (titleSchedule != null)
                    schedule = titleSchedule.getElementsByClass("inline-list").text();
                Element titleGrade = q.getElementsByClass("vacancy-card__skills").first();
                String grade = "";
                if (titleGrade != null)
                    grade = titleGrade.getElementsByClass("inline-list").text();

                String url = ("https://career.habr.com" + titleName.attr("href"));

                vacancyInformation.setLocation(location);
                vacancyInformation.setName(name);
                vacancyInformation.setCompany(company);
                vacancyInformation.setUrl(url);
                vacancyInformation.setGrade(grade);
                vacancyInformation.setSchedule(schedule);
                vacancyInformation.setType(vacancyType);
               // vacancyInformation.setSite(NameSite.HABR);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();

       // element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }
}
