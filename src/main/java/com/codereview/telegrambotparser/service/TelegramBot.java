package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.config.BotConfig;
import com.codereview.telegrambotparser.job.HHParser;
import com.codereview.telegrambotparser.job.HabrParser;
import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.Vacancy;
import com.codereview.telegrambotparser.model.VacancyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    final VacancyService service;

    public TelegramBot(BotConfig config, VacancyService service) {
        this.config = config;
        this.service = service;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendWelcomeMessage(chatId);
            } else if (messageText.equals("/vacancies")) {
                sendVacancies(chatId);
            }
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeMessage = "Привет! Я бот, который может уведомлять о новых вакансиях по следующим направлениям:\n" +
                "- Python\n" +
                "- Java\n" +
                "- JavaScript\n" +
                "- Data Science\n" +
                "- QA\n" +
                "- C#\n\n" +
                "Для получения списка вакансий, отправь /vacancies";
        sendMessageToChat(chatId, welcomeMessage);
    }

    //@Scheduled(cron = "${cron.scheduler}")
    //@Scheduled(cron = "@hourly")
    @Scheduled(cron = "0 2 * * * *")
    private void sendVacancies(long chatId) {
        // Здесь можно добавить код для парсинга вакансий из различных источников
        // и форматирования списка вакансий

        StringBuilder vacanciesMessage = new StringBuilder("Список новых вакансий:\n");
        vacanciesMessage.append(NameSite.HH).append(" ").append(VacancyType.JAVA).append("\n");
        vacanciesMessage.append(NameSite.HABR).append(" ").append(VacancyType.C_SHARP).append("\n");
        vacanciesMessage.append(NameSite.HABR).append(" ").append(VacancyType.JAVA).append("\n");

        // Отправка сообщения в чат
        sendMessageToChat(chatId, vacanciesMessage.toString());

        HHParser hhParser = new HHParser("Java");
        service.addAll(hhParser.start());
        List<Vacancy> vacancies = service.getByTypeAndSiteForLastHour(VacancyType.JAVA, NameSite.HH);
        getMessageListVacancies(chatId, vacancies);

        HabrParser habrParserCharp = new HabrParser("C#");
        service.addAll(habrParserCharp.start());
        vacancies = service.getByTypeAndSite(VacancyType.C_SHARP, NameSite.HABR);
        getMessageListVacancies(chatId, vacancies);

        HabrParser habrParserJava = new HabrParser("Java");
        service.addAll(habrParserJava.start());
        vacancies = service.getByTypeAndSite(VacancyType.JAVA, NameSite.HABR);
        getMessageListVacancies(chatId, vacancies);

    }

    private void getMessageListVacancies(long chatId, List<Vacancy> vacancies) {
        StringBuilder textMessage = new StringBuilder();
        sendMessageToChat(chatId, "Всего вакансий: " + vacancies.size());
        for (int index = 0; index < vacancies.size(); index++) {
            if ((index != 0 && index % 5 == 0)) {
                sendMessageToChat(chatId, textMessage.toString());
                textMessage = new StringBuilder();
            }
            textMessage.append(vacancies.get(index).getId()).append(". ");
            textMessage.append(vacancies.get(index).getName()).append(" ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getCompany()).append(" ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getLocation()).append(" ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getGrade()).append(" ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getSchedule()).append(" ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getDateTime()).append(" ");
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getUrl());
            textMessage.append(System.lineSeparator().repeat(1));
            textMessage.append(System.lineSeparator().repeat(1));

            if (index == vacancies.size() - 1) {
                sendMessageToChat(chatId, textMessage.toString());
            }
        }
    }

    private void sendMessageToChat(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
