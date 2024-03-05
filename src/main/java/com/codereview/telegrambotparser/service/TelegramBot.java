package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.config.BotConfig;
import com.codereview.telegrambotparser.job.HHParser;
import com.codereview.telegrambotparser.job.HabrParser;
import com.codereview.telegrambotparser.job.JobbyParser;
import com.codereview.telegrambotparser.job.HexletParser;
import com.codereview.telegrambotparser.model.NameSite;
import com.codereview.telegrambotparser.model.UserChat;
import com.codereview.telegrambotparser.model.Vacancy;
import com.codereview.telegrambotparser.model.VacancyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
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
            "- C#\n\n";//+
    //"Для начала давай пройдём регистрацию:";
    private final String MESSAGE_1 = "Список новых вакансий.";
    private final String MESSAGE_2 = "По направлению ";
    private final String MESSAGE_3 = "c сайта ";
    private final String MESSAGE_4 = "Всего вакансий: ";
    private final String MESSAGE_5 = "Введите свою почту: ";
    //private final String MESSAGE_6 = "Введите интересующее направление: ";
    private final String MESSAGE_7 = "Для получения списка вакансий, отправь /vacancies";
    private final String MESSAGE_8 = "регистрация прошла успешно";
    private final String SEARCH_MESSAGE_1 = "[ссылка на вакансию](";
    private final String SEARCH_MESSAGE_2 = "/)";
    private final String START = "/start";
    private final String VACANCIES = "/vacancies";
    private final String START_DESCRIPTION = "get a welcome message";
    private final String VACANCIES_DESCRIPTION = "get all vacancies by filter";
    private int numberMessages = 0;
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

    final BotConfig config;

    final VacancyService vacancyService;
    final UserService userService;

    public TelegramBot(BotConfig config, VacancyService vacancyService, UserService userService) {
        super(config.getToken());
        this.config = config;
        this.vacancyService = vacancyService;
        this.userService = userService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand(START, START_DESCRIPTION));
//      listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand(VACANCIES, VACANCIES_DESCRIPTION));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                switch (messageText) {
                    case START -> {
                        execute(message(update.getMessage(), WELCOME_MESSAGE));
                        UserChat userChat = userService.getByChatId(update.getMessage().getChatId());
                        if (userChat == null) {
                            execute(message(update.getMessage(), MESSAGE_5));
                            numberMessages = 1;
                        } else {
                            execute(message(update.getMessage(), MESSAGE_7));
                        }
                    }
                    case VACANCIES -> sendVacancies(chatId);
                    case "Фильр" -> message(update.getMessage(), "Filter press button");
                    default -> {
                        if (numberMessages == 1) {
                            if (userService.isValidEmail(messageText)) {
                                registrationUser(chatId, messageText, update.getMessage().getChat().getUserName());
                                execute(message(update.getMessage(), MESSAGE_8));
                                execute(message(update.getMessage(), MESSAGE_7));
                                numberMessages = 0;
                            } else {
                                execute(message(update.getMessage(), "введен неправильный формат почты, повторите снова"));
                                numberMessages = 1;
                            }
                        }
                    }
                }
            } else if (update.hasCallbackQuery()) {
                String str = update.getCallbackQuery().getData();
                parseButton(str, update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void registrationUser(Long chatId, String email, String name) {
        UserChat userChat = new UserChat();
        userChat.setChatId(chatId);
        userChat.setEmail(email);
        userChat.setType(VacancyType.JAVA);
        userChat.setName(name);
        userService.registration(userChat);
    }

    private void sendVacancies(long chatId) {
        parsingVacanciesBySites();
        for (VacancyType type : VacancyType.values()) {
            for (NameSite site : NameSite.values()) {
                log.info("parsing vacancies {} on the site {}", type, site);
                List<Vacancy> vacancies = vacancyService.getByTypeAndSiteForLastHour(type, site);
                getMessageListVacancies(chatId, vacancies, type, site);
            }
        }
    }

    @Async("jobExecutor")
    @Scheduled(cron = "0 0 * * * *")
    public void sendVacanciesSchedule() throws InterruptedException {
        List<UserChat> userChatList = userService.getAll();
        for (UserChat userChat : userChatList) {
            long chatId = userChat.getChatId();
            VacancyType type = userChat.getType();
            for (NameSite site : NameSite.values()) {
                log.info("Vacancies {} on the site {} for user {} ", type, site, chatId);
                List<Vacancy> vacancies = vacancyService.getByTypeAndSiteForLastHour(type, site);
                getMessageListVacancies(chatId, vacancies, type, site);
                Thread.sleep(5000);
            }
        }
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
            if (!schedule.isEmpty()) {
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

    private void parseButton(String selectButton, CallbackQuery callbackQuery) {
        try {
            if (selectButton.contains("Фильтр")) {
                message(callbackQuery.getMessage(), selectButton);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public SendMessage message(Message message, String update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        setButtonFilter(sendMessage);
        sendMessage.setText(update);
        return sendMessage;
    }

    private void setButtonFilter(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        KeyboardButton button = new KeyboardButton("Фильтр");
        WebAppInfo webAppInfo = new WebAppInfo("https://test-bot-phi-ashen.vercel.app/");

        button.setWebApp(webAppInfo);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(button);
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
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

    @Async("jobExecutor")
    @Scheduled(cron = "0 0 * * * *")
    public void parsingVacanciesBySites() {
        for (VacancyType type : VacancyType.values()) {
            log.info("parsing vacancies {} on the site Head Hanter", type);
            vacancyService.addAll(new HHParser(type).start());
            log.info("parsing vacancies {} on the site Habr", type);
            vacancyService.addAll(new HabrParser(type).start());
            log.info("parsing vacancies {} on the site Jobby", type);
            vacancyService.addAll(new JobbyParser(type).start());
            log.info("parsing vacancies {} on the site Hexlet", type);
            vacancyService.addAll(new HexletParser(type).start());
        }
    }
}
