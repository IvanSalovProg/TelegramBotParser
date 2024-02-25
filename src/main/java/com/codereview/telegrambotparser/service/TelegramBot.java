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
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final String WELCOME_MESSAGE = "Привет! Я бот, который может уведомлять о новых вакансиях по следующим направлениям:\n" +
            "- Python\n" +
            "- Java\n" +
            "- JavaScript\n" +
            "- Data Science\n" +
            "- QA\n" +
            "- C#\n\n" +
            "Для получения списка вакансий, отправь /vacancies";
    private final String MESSAGE_1 = "Список новых вакансий.";
    private final String MESSAGE_2 = "По направлению ";
    private final String MESSAGE_3 = "c сайта ";
    private final String MESSAGE_4 = "Всего вакансий: ";
    private final String SEARCH_MESSAGE_1 = "[ссылка на вакансию](";
    private final String SEARCH_MESSAGE_2 = "/)";
    private final String START = "/start";
    private final String VACANCIES = "/vacancies";
    private final String START_DESCRIPTION = "get a welcome message";
    private final String VACANCIES_DESCRIPTION = "get all vacancies by filter";

    final BotConfig config;

    final VacancyService service;

    public TelegramBot(BotConfig config, VacancyService service) {
        this.config = config;
        this.service = service;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand(START, START_DESCRIPTION));
//      listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand(VACANCIES, VACANCIES_DESCRIPTION));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
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

            if (messageText.equals(START)) {
                sendMessageToChat(chatId, WELCOME_MESSAGE);
            } else if (messageText.equals(VACANCIES)) {
                sendVacancies(chatId);
            }
        }
    }

    //@Scheduled(cron = "${cron.scheduler}")
    //@Scheduled(cron = "@hourly")
   // @Scheduled(cron = "0 2 * * * *")
    private void sendVacancies(long chatId) {
        parsingVacanciesBySites();

        VacancyType type = VacancyType.JAVA;
        NameSite site = NameSite.HH;
        List<Vacancy> vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.CSHARP;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.DATASCIENCE;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.JAVASCRIPT;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.PYTHON;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.QA;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.CSHARP;
        site = NameSite.HABR;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);

        type = VacancyType.JAVA;
        vacancies = service.getByTypeAndSiteForLastHour(type, site);
        getMessageListVacancies(chatId, vacancies, type, site);
    }

    private void getMessageListVacancies(long chatId, List<Vacancy> vacancies, VacancyType type, NameSite site) {
        StringBuilder textMessage = new StringBuilder();
        textMessage.append(MESSAGE_1).append(System.lineSeparator().repeat(1));
        textMessage.append(MESSAGE_2).append(type.name().toLowerCase()).append(" ");
        textMessage.append(MESSAGE_3).append(new ReferenceManager().getNameSite(site));
        textMessage.append(System.lineSeparator().repeat(2));
        sendMessageToChat(chatId, MESSAGE_4 + vacancies.size());
        for (int index = 0; index < vacancies.size(); index++) {
            if ((index != 0 && index % 10 == 0)) {
                sendMessageToChat(chatId, textMessage.toString());
                textMessage = new StringBuilder();
            }
            textMessage.append(index + 1).append(". ").append(vacancies.get(index).getName()).append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getCompany()).append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getLocation()).append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getGrade()).append(System.lineSeparator().repeat(1));
            String schedule = vacancies.get(index).getSchedule();
            if(!schedule.isEmpty()) {
                textMessage.append(schedule).append(System.lineSeparator().repeat(1));
            }
            textMessage.append(vacancies.get(index).getDateTime()).append(System.lineSeparator().repeat(1));
            textMessage.append(SEARCH_MESSAGE_1).append(vacancies.get(index).getUrl()).append(SEARCH_MESSAGE_2);
            textMessage.append(System.lineSeparator().repeat(2));

            if (index == vacancies.size() - 1) {
                sendMessageToChat(chatId, textMessage.toString());
            }
        }
    }

    private void sendMessageToChat(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.disableWebPagePreview();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 2 * * * *")
    private void parsingVacanciesBySites() {
        for(VacancyType type : VacancyType.values()) {
            log.info("parsing vacancies");
            service.addAll(new HHParser(type).start());
            if(type.equals(VacancyType.JAVA) || type.equals(VacancyType.CSHARP)) {
                service.addAll(new HabrParser(type).start());
            }
        }
    }
}
