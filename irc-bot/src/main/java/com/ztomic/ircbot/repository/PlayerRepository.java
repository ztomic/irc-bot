package com.ztomic.ircbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ztomic.ircbot.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

	List<Player> findByServerAndChannelIgnoreCaseAndLastAnsweredIsNotNull(String server, String channel);
	List<Player> findByServerAndChannelIgnoreCase(String server, String channel);
	Player findByServerAndChannelIgnoreCaseAndNickIgnoreCase(String server, String channel, String nick);
	
}
