package com.codereview.telegrambotparser.controller;

/*import com.codereview.telegrambotparser.dto.UserChatDTO;
import com.codereview.telegrambotparser.model.UserChat;
import com.codereview.telegrambotparser.model.VacancyType;
import com.codereview.telegrambotparser.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = UserController.REST_URL)
@CrossOrigin*/
public class UserController {

   /* public static final String REST_URL = "/api/telegram-bot";
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PutMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody UserChatDTO userChatDTO, @PathVariable String name) {
        log.info("update user {} with id={}", name);
*//*        if (userChatDTO.isNew()) {
            userChatDTO.setId(id);
        } else if (userChatDTO.id() != id) {
            throw new RuntimeException(userChatDTO.getClass().getSimpleName() + " must has id=" + id);
        }*//*
        UserChat user = service.getByName(name);
        user.setType(userChatDTO.getType());
        service.update(user);
        log.info("User data updated");
    }

    @GetMapping("/{name}")
    public UserChatDTO get(@PathVariable String name) {
        log.info("get user {}", name);
        UserChat user = service.getByName(name);
        UserChatDTO userChatDTO = new UserChatDTO();
        userChatDTO.setId(user.getId());
        //userChatDTO.setGrade(user.getGrade());
        userChatDTO.setType(user.getType());
        return userChatDTO;
    }*/
}
