package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.model.UserChat;
import com.codereview.telegrambotparser.model.VacancyType;
import com.codereview.telegrambotparser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void registration(Long chatId, String email, String name) {
        log.info("registration new user {}", name);
        List<UserChat> userChats = repository.findByEmail(email);
        if(userChats.isEmpty()) {
            UserChat newUser = new UserChat();
            newUser.setChatId(chatId);
            newUser.setEmail(email);
            newUser.setName(name);
            repository.save(newUser);
        }
    }

    public void update(UserChat userChat) {
        log.info("update user {}", userChat);
        if(!userChat.isNew()) {
            repository.save(userChat);
        }
    }

    public void updateType(long chatId, VacancyType type) {
        log.info("update vacancies type on {} for user {}", type, chatId);
        UserChat user = repository.getByChatId(chatId);
        if(user != null) {
            user.setType(type);
            repository.save(user);
        }
    }

    public UserChat getByChatId(long chatId) {
        log.info("get user by chatId = {}", chatId);
       return repository.getByChatId(chatId);
    }

    public boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public List<UserChat> getAll() {
        log.info("get all users");
        return repository.findAll();
    }

    public UserChat getByName(String name) {
        log.info("get user by name {}", name);
        return repository.findByName(name);
    }
}
