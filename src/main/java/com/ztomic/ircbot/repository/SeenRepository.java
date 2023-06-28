package com.ztomic.ircbot.repository;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ztomic.ircbot.model.Seen;

public interface SeenRepository extends JpaRepository<Seen, Long> {

	Seen findByServerAndNickIgnoreCase(String server, String nick);
	
	List<Seen> findAllByServerAndNickIgnoreCase(String server, String nick, Sort sort);

	Stream<Seen> findAllByServerIsNotNull(Sort sort);
	
}
