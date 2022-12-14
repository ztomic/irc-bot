package com.ztomic.ircbot.repository;

import com.ztomic.ircbot.model.QuestionError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionErrorRepository extends JpaRepository<QuestionError, Long> {
	
}
