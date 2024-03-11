package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.config.BotConfig;
import com.codereview.telegrambotparser.parser.HHParser;
import com.codereview.telegrambotparser.parser.HabrParser;
import com.codereview.telegrambotparser.parser.JobbyParser;
import com.codereview.telegrambotparser.parser.HexletParser;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
            "- C#\n\n";
    private final String MESSAGE_1 = "Список новых вакансий.";
    private final String MESSAGE_2 = "По направлению ";
    private final String MESSAGE_3 = "c сайта ";
    private final String MESSAGE_4 = "Всего вакансий: ";
    private final String MESSAGE_5 = "Введите свою почту: ";
    private final String MESSAGE_7 = "Для получения списка вакансий, отправь /vacancies";
    private final String MESSAGE_8 = "регистрация прошла успешно";
    private final String SEARCH_MESSAGE_1 = "[ссылка на вакансию](";
    private final String SEARCH_MESSAGE_2 = "/)";
    private final String START = "/start";
    private final String VACANCIES = "/vacancies";
    private final String UPDATE_VACANCY_TYPE = "/update_vacancy_type";
    private final String START_DESCRIPTION = "старт";
    private final String VACANCIES_DESCRIPTION = "получение всех вакансий за час";
    private final String UPDATE_VACANCY_TYPE_DESCRIPTION = "сменить направление вакансий";
    private final String ERROR_MAIL_MESSAGE = "введен неправильный формат почты, повторите снова";
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
        listOfCommands.add(new BotCommand(UPDATE_VACANCY_TYPE, UPDATE_VACANCY_TYPE_DESCRIPTION));
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
                    case VACANCIES -> sendVacanciesByClick(chatId);
                    case UPDATE_VACANCY_TYPE -> setButtonsVacancyType(update.getMessage().getChatId());
                    default -> {
                        if (numberMessages == 1) {
                            if (userService.isValidEmail(messageText)) {
                                userService.registration(chatId, messageText, update.getMessage().getChat().getUserName());
                                execute(message(update.getMessage(), MESSAGE_8));
                                numberMessages = 0;
                                setButtonsVacancyType(chatId);
                            } else {
                                execute(message(update.getMessage(), ERROR_MAIL_MESSAGE));
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

    private void sendVacanciesByClick(long chatId) throws InterruptedException {
        //parsingVacanciesBySites();
        UserChat user = userService.getByChatId(chatId);
        sendVacancies(chatId, user.getType());
    }

    @Async("jobExecutor")
    @Scheduled(cron = "0 0 * * * *")
    public void sendVacanciesBySchedule() throws InterruptedException {
        List<UserChat> userChatList = userService.getAll();
        for (UserChat userChat : userChatList) {
            sendVacancies(userChat.getChatId(), userChat.getType());
        }
    }

    private void sendVacancies(long chatId, VacancyType type) throws InterruptedException {
        for (NameSite site : NameSite.values()) {
            log.info("Vacancies {} on the site {} for user {} ", type, site, chatId);
            List<Vacancy> vacancies = vacancyService.getByTypeAndSiteForLastHour(type, site);
            getMessageListVacancies(chatId, vacancies, type, site);
            Thread.sleep(5000);
        }
    }

    private void getMessageListVacancies(long chatId, List<Vacancy> vacancies, VacancyType type, NameSite site) {
        StringBuilder textMessage = new StringBuilder();
        String nameSite = new ReferenceManager().getNameReference(site.name());
        String vacancyType = new ReferenceManager().getNameReference(type.name());
        textMessage.append(MESSAGE_1).append(System.lineSeparator().repeat(1));
        textMessage.append(MESSAGE_2).append(vacancyType).append(" ");
        textMessage.append(MESSAGE_3).append(nameSite);
        textMessage.append(System.lineSeparator().repeat(2));
        for (int index = 0; index < vacancies.size(); index++) {
            if ((index != 0 && index % 10 == 0)) {
                sendMessageToChat(chatId, textMessage.toString());
                textMessage = new StringBuilder();
                textMessage.append(MESSAGE_1).append(System.lineSeparator().repeat(1));
                textMessage.append(MESSAGE_2).append(vacancyType).append(" ");
                textMessage.append(MESSAGE_3).append(nameSite);
                textMessage.append(System.lineSeparator().repeat(2));
            }
            textMessage.append(index + 1).append(". ").append(vacancies.get(index).getName()).append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getCompany()).append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getLocation()).append(System.lineSeparator().repeat(1));
            textMessage.append(vacancies.get(index).getGrade()).append(System.lineSeparator().repeat(1));
            String schedule = vacancies.get(index).getSchedule();
            if (!schedule.isEmpty()) {
                textMessage.append(schedule).append(System.lineSeparator().repeat(1));
            }
            //textMessage.append(vacancies.get(index).getDateTime()).append(System.lineSeparator().repeat(1));
            textMessage.append(SEARCH_MESSAGE_1).append(vacancies.get(index).getUrl()).append(SEARCH_MESSAGE_2);
            textMessage.append(System.lineSeparator().repeat(2));

            if (index == vacancies.size() - 1) {
                sendMessageToChat(chatId, textMessage.toString());
            }
        }
    }

    private void parseButton(String selectButton, CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        try {
            switch (selectButton) {
                case "JAVA" -> {
                    userService.updateType(chatId, VacancyType.JAVA);
                    execute(message(callbackQuery.getMessage(), "направление вакансий обновлено"));
                    execute(message(callbackQuery.getMessage(), MESSAGE_7));
                }
                case "PYTHON" -> {
                    userService.updateType(chatId, VacancyType.PYTHON);
                    execute(message(callbackQuery.getMessage(), "направление вакансий обновлено"));
                    execute(message(callbackQuery.getMessage(), MESSAGE_7));
                }
                case "JAVASCRIPT" -> {
                    userService.updateType(chatId, VacancyType.JAVASCRIPT);
                    execute(message(callbackQuery.getMessage(), "направление вакансий обновлено"));
                    execute(message(callbackQuery.getMessage(), MESSAGE_7));
                }
                case "DATASCIENCE" -> {
                    userService.updateType(chatId, VacancyType.DATASCIENCE);
                    execute(message(callbackQuery.getMessage(), "направление вакансий обновлено"));
                    execute(message(callbackQuery.getMessage(), MESSAGE_7));
                }
                case "QA" -> {
                    userService.updateType(chatId, VacancyType.QA);
                    execute(message(callbackQuery.getMessage(), "направление вакансий обновлено"));
                    execute(message(callbackQuery.getMessage(), MESSAGE_7));
                }
                case "CSHARP" -> {
                    userService.updateType(chatId, VacancyType.CSHARP);
                    execute(message(callbackQuery.getMessage(), "направление вакансий обновлено"));
                    execute(message(callbackQuery.getMessage(), MESSAGE_7));
                }
                default -> execute(message(callbackQuery.getMessage(), "введена неизвестная кнопка, повторите снова"));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public SendMessage message(Message message, String update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
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

    private void setButtonsVacancyType(long chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите направление вакансий:");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();

        InlineKeyboardButton JavaButton = new InlineKeyboardButton();
        JavaButton.setText("Java");
        JavaButton.setCallbackData("JAVA");

        InlineKeyboardButton PythonButton = new InlineKeyboardButton();
        PythonButton.setText("Python");
        PythonButton.setCallbackData("PYTHON");

        InlineKeyboardButton JavaScriptButton = new InlineKeyboardButton();
        JavaScriptButton.setText("JavaScript");
        JavaScriptButton.setCallbackData("JAVASCRIPT");

        rowInLine1.add(JavaButton);
        rowInLine1.add(PythonButton);
        rowInLine1.add(JavaScriptButton);

        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();

        InlineKeyboardButton DataScienceButton = new InlineKeyboardButton();
        DataScienceButton.setText("Data Science");
        DataScienceButton.setCallbackData("DATASCIENCE");

        InlineKeyboardButton QAButton = new InlineKeyboardButton();
        QAButton.setText("QA");
        QAButton.setCallbackData("QA");

        InlineKeyboardButton CSharpButton = new InlineKeyboardButton();
        CSharpButton.setText("C#");
        CSharpButton.setCallbackData("CSHARP");

        rowInLine2.add(DataScienceButton);
        rowInLine2.add(QAButton);
        rowInLine2.add(CSharpButton);

        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);

        markupInLine.setKeyboard(rowsInLine);
        sendMessage.setReplyMarkup(markupInLine);

        execute(sendMessage);
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
