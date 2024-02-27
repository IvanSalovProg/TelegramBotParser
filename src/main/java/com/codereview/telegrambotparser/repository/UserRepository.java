package com.codereview.telegrambotparser.repository;

import com.codereview.telegrambotparser.model.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<UserChat, Long> {

    @Query("SELECT u FROM UserChat u WHERE u.email =:email")
    List<UserChat> findByEmail(String email);
}
