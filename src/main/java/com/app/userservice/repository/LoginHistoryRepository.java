package com.app.userservice.repository;

import com.app.userservice.entity.user.LoginHistory;
import com.app.userservice.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserOrderByLoginTimeDesc(User user);
    List<LoginHistory> findTop10ByUserOrderByLoginTimeDesc(User user);
}