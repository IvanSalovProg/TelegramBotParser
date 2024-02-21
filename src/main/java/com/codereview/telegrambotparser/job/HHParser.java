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
public class HHParser extends VacancyParser{

    private final String URL_PART1 = "https://hh.ru/search/vacancy?enable_snippets=true&ored_clusters=true&search_field=name&text=";
    private final String URL_PART2 = "&search_period=1&disableBrowserCache=true&page=";
    private final String HH_URL;
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public HHParser(String vacancyType) {
        HH_URL = URL_PART1 + vacancyType.toLowerCase() + URL_PART2;
        this.vacancyType = VacancyType.valueOf(vacancyType.toUpperCase());
    }

    public List<Vacancy> start() {
        return parser();
    }

    public String nextPageUrl(int page) {
        return HH_URL + page;
    }

    @Override
    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 20) return new ArrayList<>();

        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass("vacancy-serp-item__layout");

        List<Vacancy> element = elements.stream().map(q -> {
            Vacancy vacancyInformation = new Vacancy();
            Element titleName = q.getElementsByClass("bloko-link").first();
            if (titleName != null) {
                String name = "";
                name = titleName.getElementsByTag("a").text();
                Element titleCompany = q.getElementsByClass("vacancy-serp-item__meta-info-company").first();
                String company = "";
                if (titleCompany != null)
                    company = titleCompany.getElementsByTag("a").text();
                Element titleLocation = q.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy-address").first();
                String location = "";
                if (titleLocation != null)
                    location = titleLocation.getElementsByClass("bloko-text").text();
                Element titleSchedule = q.getElementsByClass("labels--CBiQJ5KZ2PKw9wf0Aizk").first();
                String schedule = "";
                if (titleSchedule != null)
                    schedule = titleSchedule.getElementsByClass("label_light-violet--mfqJrKkFOboQUFsgaJp2").text();
                Element titleGrade = q.getElementsByClass("bloko-h-spacing-container_base-0").first();
                String grade = "";
                if (titleGrade != null)
                    grade = titleGrade.getElementsByClass("bloko-text").text();

                String url = titleName.attr("href");

                vacancyInformation.setLocation(location);
                vacancyInformation.setName(name);
                vacancyInformation.setCompany(company);
                vacancyInformation.setUrl(url);
                vacancyInformation.setGrade(grade);
                vacancyInformation.setSchedule(schedule);
                vacancyInformation.setType(vacancyType);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();

        //element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();

    }
}
