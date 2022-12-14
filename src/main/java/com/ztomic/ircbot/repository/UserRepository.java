package com.ztomic.ircbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ztomic.ircbot.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByServerAndNickIgnoreCase(String server, String nick);
	
}
