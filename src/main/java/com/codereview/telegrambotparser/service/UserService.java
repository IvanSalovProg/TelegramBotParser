package com.codereview.telegrambotparser.service;

import com.codereview.telegrambotparser.model.UserChat;
import com.codereview.telegrambotparser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void registration(UserChat userChatDTO) {
        List<UserChat> userChats = repository.findByEmail(userChatDTO.getEmail());
        if(userChats.isEmpty()) {
            repository.save(userChatDTO);
        }
    }

    public void update(UserChat userChat) {
        if(!userChat.isNew()) {
            repository.save(userChat);
        }
    }

    public UserChat get(long id) {
        return repository.getById(id);
    }
}
