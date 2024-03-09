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
public class HexletParser extends VacancyParser{
    private final String URL_PART1 = "https://cv.hexlet.io/ru/vacancy_filters/direction-";
    private final String URL_PART2 = "";
    private final String C_SHARP = "c%23";
    private final String JAVA_ = "java";
    private final String DATA_SCIENCE = "da";
    private final String QA_ = "тестировщик";
    private final String JAVASCRIPT_ = "js";
    private final String PYTHON_ = "python";

    private final String Hexlet_URL;
    private final String PARS_VACANCIES = "card-body";
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public HexletParser(VacancyType vacancyType) {
        if(vacancyType.equals(VacancyType.CSHARP)) {
            Hexlet_URL = URL_PART1 + C_SHARP + URL_PART2;
        } else if(vacancyType.equals(VacancyType.DATASCIENCE)) {
            Hexlet_URL = URL_PART1 + DATA_SCIENCE + URL_PART2;
        } else if(vacancyType.equals(VacancyType.JAVA)) {
            Hexlet_URL = URL_PART1 + JAVA_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.QA)) {
            Hexlet_URL = URL_PART1 + QA_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.JAVASCRIPT)) {
            Hexlet_URL = URL_PART1 + JAVASCRIPT_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.PYTHON)) {
            Hexlet_URL = URL_PART1 + PYTHON_ + URL_PART2;
        }
        else {Hexlet_URL = URL_PART1 + vacancyType.name().toLowerCase() + URL_PART2;}

        this.vacancyType = vacancyType;

    }

    public List<Vacancy> start() {
        return parser();
    }

    public String nextPageUrl(int page) {
        return Hexlet_URL + page;
    }

    @Override
    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 50) return new ArrayList<>();

        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass(PARS_VACANCIES);

        List<Vacancy> element = elements.stream().map(q -> {
            Vacancy vacancyInformation = new Vacancy();
            Element titleName = q.getElementsByClass("card-title").first();
            if (titleName != null) {
                String name = "";
                name = titleName.getElementsByTag("a").text();
                Element titleCompany = q.getElementsByClass("").first();
                String company = "";
                if (titleCompany != null)
                    company = titleCompany.getElementsByTag("a").text();
                Element titleLocation = q.getElementsByClass("").first();
                String location = "";
                if (titleLocation != null)
                    location = titleLocation.getElementsByClass("").text();
                Element titleSchedule = q.getElementsByClass("").first();
                String schedule = "";
                if (titleSchedule != null)
                    schedule = titleSchedule.getElementsByClass("card-title").text();
                Element titleGrade = q.getElementsByClass("").first();
                String grade = "";
                if (titleGrade != null)
                    grade = titleGrade.getElementsByClass("").text();

                String url = "https://cv.hexlet.io/ru/vacancies" + titleName.attr("href");

                vacancyInformation.setLocation(location);
                vacancyInformation.setName(name);
                vacancyInformation.setCompany(company);
                vacancyInformation.setUrl(url);
                vacancyInformation.setGrade(grade);
                vacancyInformation.setSchedule(schedule);
                vacancyInformation.setType(vacancyType);
                vacancyInformation.setSite(NameSite.HEXLET);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();


        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }
}
