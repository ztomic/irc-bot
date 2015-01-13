package com.ztomic.ircbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ztomic.ircbot.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("select u from User u where (u.server = ?1) and UPPER(u.nick) = UPPER(?2)")
	public User findByServerAndNick(String server, String nick);
	
}
