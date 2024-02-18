package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.config.BotConfig;
import com.codereview.telegrambotparser.job.VacancyParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
   final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
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

    private void sendVacancies(long chatId) {
        // Здесь можно добавить код для парсинга вакансий из различных источников
        // и форматирования списка вакансий

        String vacanciesMessage = "Список новых вакансий по Java на hh.ru:\n";

        sendMessageToChat(chatId, vacanciesMessage);

        String urlBase = "https://novosibirsk.hh.ru/search/vacancy?text=java&area=4&page=";
        VacancyParser vacancyParser = new VacancyParser(urlBase);
        vacanciesMessage = vacancyParser.start();

        sendMessageToChat(chatId, vacanciesMessage);
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
