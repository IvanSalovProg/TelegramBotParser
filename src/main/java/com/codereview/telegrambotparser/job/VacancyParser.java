package com.codereview.telegrambotparser.job;

import com.codereview.telegrambotparser.model.Vacancy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VacancyParser {

    private String urlBase;
    private int positionCounter = 1;
    private static String key;

    public VacancyParser(String urlBase) {
        this.urlBase = urlBase;
    }

    public String start() {
        List<Vacancy> vacancies = parser("Java");
        String result = "";
        System.out.println("Список вакансий по направлению Java:");
        for (Vacancy vacancy : vacancies) {
            System.out.println(vacancy.getPosition() + "- " + vacancy.getName() + " " + vacancy.getCompany());
            System.out.println(vacancy.getDescription());
            System.out.println(vacancy.getUrl());
            result += vacancy.getPosition() + ". " + vacancy.getName() + " " + vacancy.getCompany() + " " + vacancy.getUrl() + "\n\r";
        }
        return result;
    }

    public List<Vacancy> parser(String message) {
        List<Vacancy> list = new ArrayList<>();
        try {
            key = message.replace(" ", "%20");
            list = new ArrayList<>(startParser());
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return list;
    }

    private List<Vacancy> startParser() {
        int page = 0;
        List<Vacancy> parseData = new ArrayList<>();
        while (true) {
            String pageUrl = nextPageUrl(page);
            page++;
            List<Vacancy> elements = getElements(pageUrl);
            parseData.addAll(elements);
            if (elements.isEmpty()) {
                break;
            }
        }
        return parseData;
    }

    public String nextPageUrl(int page) {
        return urlBase + key + "&p=" + ++page;
    }

    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 5) return new ArrayList<>();

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

    public static Document getHtml(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .referrer("https://google.com")
                    .get();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return doc;
    }
}
