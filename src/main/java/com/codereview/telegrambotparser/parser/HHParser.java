package com.codereview.telegrambotparser.parser;

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
public class HHParser extends VacancyParser{

    private final String URL_PART1 = "https://hh.ru/search/vacancy?enable_snippets=true&ored_clusters=true&search_field=name&text=";
    private final String URL_PART2 = "&search_period=1&disableBrowserCache=true&page=";
    private final String C_SHARP = "c%23";
    private final String DATA_SCIENCE = "data+science";
    private final String HH_URL;
    private final String PARS_VACANCIES = "vacancy-serp-item__layout";
    private final String PARS_NAME = "bloko-link";
    private final String PARS_COMPANY = "vacancy-serp-item__meta-info-company";
    private final String PARS_SCHEDULE_1 = "labels--CBiQJ5KZ2PKw9wf0Aizk";
    private final String PARS_SCHEDULE_2 = "label_light-violet--mfqJrKkFOboQUFsgaJp2";
    private final String PARS_LOCATION = "vacancy-serp__vacancy-address";
    private final String PARS_GRADE = "bloko-h-spacing-container_base-0";
    private final String TAG = "a";
    private final String NAME_ATTRIBUTE = "data-qa";
    private final String BLOKO_TEXT = "bloko-text";
    private final String ATTR = "href";
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public HHParser(VacancyType vacancyType) {
        if(vacancyType.equals(VacancyType.CSHARP)) {
            HH_URL = URL_PART1 + C_SHARP + URL_PART2;
        } else if(vacancyType.equals(VacancyType.DATASCIENCE)) {
            HH_URL = URL_PART1 + DATA_SCIENCE + URL_PART2;
        } else {
            HH_URL = URL_PART1 + vacancyType.name().toLowerCase() + URL_PART2;
        }
        this.vacancyType = vacancyType;
    }

    public List<Vacancy> start() {
        return parser();
    }

    public String nextPageUrl(int page) {
        return HH_URL + page;
    }

    @Override
    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 50) return new ArrayList<>();

        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass(PARS_VACANCIES);

        List<Vacancy> vacancies = elements.stream().map(element -> {
            Vacancy vacancyInformation = new Vacancy();
            Element titleName = element.getElementsByClass(PARS_NAME).first();
            if (titleName != null) {
                vacancyInformation.setName(titleName.getElementsByTag(TAG).text());
                vacancyInformation.setLocation(getElementByAttributeValueAndClass(element, NAME_ATTRIBUTE, PARS_LOCATION, BLOKO_TEXT));
                vacancyInformation.setCompany(getElementByClassAndTag(element, PARS_COMPANY, TAG));
                vacancyInformation.setUrl(titleName.attr(ATTR));
                vacancyInformation.setGrade(getElementByClassAndClass(element, PARS_GRADE, BLOKO_TEXT));
                vacancyInformation.setSchedule(getElementByClassAndClass(element, PARS_SCHEDULE_1, PARS_SCHEDULE_2));
                vacancyInformation.setType(vacancyType);
                vacancyInformation.setSite(NameSite.HH);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();

        //element = element.stream().filter(q -> q.getPosition() > 0).toList();
        return (position != positionCounter) ? new ArrayList<>(vacancies) : new ArrayList<>();
    }

    private String getElementByClassAndTag(Element element, String nameClass, String nameTag) {
        Element titleCompany = element.getElementsByClass(nameClass).first();
        String string = "";
        if (titleCompany != null)
            string = titleCompany.getElementsByTag(nameTag).text();
        return string;
    }

    private String getElementByAttributeValueAndClass(Element element, String nameAttribute, String nameValue, String nameClass) {
        Element titleLocation = element.getElementsByAttributeValue(nameAttribute, nameValue).first();
        String string = "";
        if (titleLocation != null)
            string = titleLocation.getElementsByClass(nameClass).text();
        return string;
    }

    private String getElementByClassAndClass(Element element, String nameClass1, String nameClass2) {
        Element titleSchedule = element.getElementsByClass(nameClass1).first();
        String string = "";
        if (titleSchedule != null)
            string = titleSchedule.getElementsByClass(nameClass2).text();
        return string;
    }
}
