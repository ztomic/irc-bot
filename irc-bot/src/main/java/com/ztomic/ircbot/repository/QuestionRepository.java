package com.ztomic.ircbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ztomic.ircbot.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	List<Question> findByLanguageIgnoreCase(String language);
	
}
