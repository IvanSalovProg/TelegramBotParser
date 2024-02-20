package com.codereview.telegrambotparser.job;

import com.codereview.telegrambotparser.model.Vacancy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HHParser extends VacancyParser{

    private final String URL_PART1 = "https://hh.ru/search/vacancy?text=";
    private final String URL_PART2 = "java&salary=&ored_clusters=true&search_field=name&enable_snippets=true&page=";
    private final String HH_URL;
    private final String key;
    private final String vacancyType;
    private int positionCounter = 1;

    public HHParser(String vacancyType) {
        HH_URL = URL_PART1 + vacancyType.toLowerCase() + URL_PART2;
        key = vacancyType.toLowerCase().replace(" ", "%20");
        this.vacancyType = vacancyType;
    }

    public String start() {
        List<Vacancy> vacancies = parser();
        StringBuilder textMessage = new StringBuilder();
        log.info("Список вакансий по направлению: " + vacancyType);
        for (Vacancy vacancy : vacancies) {
            log.info(vacancy.getPosition() + "- " + vacancy.getName() + " " + vacancy.getCompany());
            log.info(vacancy.getDescription());
            log.info(vacancy.getUrl());
            textMessage.append(vacancy.getPosition() + ". ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancy.getName() + " ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancy.getCompany() + " ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancy.getUrl());
            textMessage.append(System.lineSeparator().repeat(1));
        }
        return textMessage.toString();
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
            Element titleElement1 = q.getElementsByClass("bloko-link").first();
            if (titleElement1 != null) {
                String element1 = "";
                element1 = titleElement1.getElementsByTag("a").text();
                Element titleElement2 = q.getElementsByClass("vacancy-serp-item__meta-info-company").first();
                String element2 = "";
                if (titleElement2 != null)
                    element2 = titleElement2.getElementsByTag("a").text();
                vacancyInformation.setPosition(positionCounter++);
                vacancyInformation.setName(element1);
                vacancyInformation.setCompany(element2);
                String url = titleElement1.attr("href");
                vacancyInformation.setUrl(url);
            }
            return vacancyInformation;
        }).toList();

        element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();

    }
}
