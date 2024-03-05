package com.codereview.telegrambotparser.controller;

import com.codereview.telegrambotparser.dto.UserChatDTO;
import com.codereview.telegrambotparser.model.UserChat;
import com.codereview.telegrambotparser.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = UserController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    public static final String REST_URL = "/api/telegram-bot";
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void update(@Valid @RequestBody UserChatDTO userChatDTO, @PathVariable long id) {
        log.info("update user {} with id={}", userChatDTO, id);
        if (userChatDTO.isNew()) {
            userChatDTO.setId(id);
        } else if (userChatDTO.id() != id) {
            throw new RuntimeException(userChatDTO.getClass().getSimpleName() + " must has id=" + id);
        }
        UserChat user = new UserChat();
        user.setId(userChatDTO.getId());
        //user.setGrade(userChatDTO.getGrade());
        user.setType(userChatDTO.getType());
        service.update(user);
        log.info("User data updated");
    }

    @GetMapping("/{id}")
    public UserChatDTO get(@PathVariable long id) {
        log.info("get user {}", id);
        UserChat user = service.get(id);
        UserChatDTO userChatDTO = new UserChatDTO();
        userChatDTO.setId(user.getId());
        //userChatDTO.setGrade(user.getGrade());
        userChatDTO.setType(user.getType());
        return userChatDTO;
    }

}
