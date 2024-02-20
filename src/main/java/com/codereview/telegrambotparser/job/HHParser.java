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

    private final String URL_PART1 = "https://hh.ru/search/vacancy?enable_snippets=true&ored_clusters=true&search_field=name&text=";
    private final String URL_PART2 = "&search_period=1&disableBrowserCache=true&page=";
    private final String HH_URL;
    private int positionCounter = 1;

    public HHParser(String vacancyType) {
        HH_URL = URL_PART1 + vacancyType.toLowerCase() + URL_PART2;
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
            Element titleElement1 = q.getElementsByClass("bloko-link").first();
            if (titleElement1 != null) {
                String element1 = "";
                element1 = titleElement1.getElementsByTag("a").text();
                Element titleElement2 = q.getElementsByClass("vacancy-serp-item__meta-info-company").first();
                String element2 = "";
                if (titleElement2 != null)
                    element2 = titleElement2.getElementsByTag("a").text();
                Element titleElement3 = q.getElementsByClass("g-user-content").first();
                String element3 = "";
                if (titleElement3 != null)
                    element3 = titleElement3.getElementsByClass("bloko-text").text();
                vacancyInformation.setPosition(positionCounter++);
                vacancyInformation.setName(element1);
                vacancyInformation.setCompany(element2);
                vacancyInformation.setDescription(element3);
                String url = titleElement1.attr("href");
                vacancyInformation.setUrl(url);
            }
            return vacancyInformation;
        }).toList();

        element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();

    }
}
