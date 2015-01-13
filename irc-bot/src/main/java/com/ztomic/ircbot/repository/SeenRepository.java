package com.ztomic.ircbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ztomic.ircbot.model.Seen;

public interface SeenRepository extends JpaRepository<Seen, Long> {

	public Seen findByServerAndNickIgnoreCase(String server, String nick);
	
}
