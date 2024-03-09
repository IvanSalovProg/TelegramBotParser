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
public class JobbyParser extends VacancyParser{
    private final String URL_PART1 = "https://jobby.ai/student_jobsearch_m?page=1&emordesk=";
    private final String URL_PART2 = "&isFirst=no";
    private final String C_SHARP = "C%23";
    private final String JAVA_ = "Java";
    private final String DATA_SCIENCE = "DATA%20SCIENCE";
    private final String QA_ = "QA";
    private final String JAVASCRIPT_ = "JAVASCRIPT";
    private final String PYTHON_ = "PYTHON";

    private final String Jobby_URL;
    private final String PARS_VACANCIES = "bubble-element.group-item";
    private int positionCounter = 1;
    private VacancyType vacancyType;

    public JobbyParser(VacancyType vacancyType) {
        if(vacancyType.equals(VacancyType.CSHARP)) {
            Jobby_URL = URL_PART1 + C_SHARP + URL_PART2;
        } else if(vacancyType.equals(VacancyType.DATASCIENCE)) {
            Jobby_URL = URL_PART1 + DATA_SCIENCE + URL_PART2;
        } else if(vacancyType.equals(VacancyType.JAVA)) {
            Jobby_URL = URL_PART1 + JAVA_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.QA)) {
            Jobby_URL = URL_PART1 + QA_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.JAVASCRIPT)) {
            Jobby_URL = URL_PART1 + JAVASCRIPT_ + URL_PART2;
        } else if(vacancyType.equals(VacancyType.PYTHON)) {
            Jobby_URL = URL_PART1 + PYTHON_ + URL_PART2;
        }
        else {Jobby_URL = URL_PART1 + vacancyType.name().toLowerCase() + URL_PART2;}

        this.vacancyType = vacancyType;

    }

    public List<Vacancy> start() {
        return parser();
    }

    public String nextPageUrl(int page) {
        return Jobby_URL + page;
    }

    @Override
    public List<Vacancy> getElements(String pageUrl) {
        if (positionCounter > 50) return new ArrayList<>();

        int position = positionCounter;
        Document doc = getHtml(pageUrl);
        Elements elements = doc.getElementsByClass(PARS_VACANCIES);

        List<Vacancy> element = elements.stream().map(q -> {
            Vacancy vacancyInformation = new Vacancy();
            Element titleName = q.getElementsByClass("bubble-element.coaArp1").first();
            if (titleName != null) {
                String name = "";
                name = titleName.getElementsByTag("a").text();
                Element titleCompany = q.getElementsByClass("bubble-element.coaMuaY0").first();
                String company = "";
                if (titleCompany != null)
                    company = titleCompany.getElementsByTag("a").text();
                Element titleLocation = q.getElementsByClass("bubble-element.coaMva0").first();
                String location = "";
                if (titleLocation != null)
                    location = titleLocation.getElementsByClass("bubble-element.coaMva0").text();
                Element titleSchedule = q.getElementsByClass("bubble-element.coaMvaY0").first();
                String schedule = "";
                if (titleSchedule != null)
                    schedule = titleSchedule.getElementsByClass("bubble-element.coaMvaY0").text();
                Element titleGrade = q.getElementsByClass("bubble-element.coaMvaY0").first();
                String grade = "";
                if (titleGrade != null)
                    grade = titleGrade.getElementsByClass("bubble-element.coaMvaY0").text();

                String url = "https://jobby.ai" + titleName.attr("href");

                vacancyInformation.setLocation(location);
                vacancyInformation.setName(name);
                vacancyInformation.setCompany(company);
                vacancyInformation.setUrl(url);
                vacancyInformation.setGrade(grade);
                vacancyInformation.setSchedule(schedule);
                vacancyInformation.setType(vacancyType);
                vacancyInformation.setSite(NameSite.JOBBY);
                positionCounter++;
            }
            return vacancyInformation;
        }).toList();


        return (position != positionCounter) ? new ArrayList<>(element) : new ArrayList<>();
    }
}
