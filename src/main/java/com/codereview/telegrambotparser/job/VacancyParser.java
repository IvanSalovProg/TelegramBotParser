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
public abstract class VacancyParser {

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

    public List<Vacancy> parser() {
        List<Vacancy> list = new ArrayList<>();
        try {
            //key = message.replace(" ", "%20");
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

    public abstract String nextPageUrl(int page);

    public abstract List<Vacancy> getElements(String pageUrl);
}
