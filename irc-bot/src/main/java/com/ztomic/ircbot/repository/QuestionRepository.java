package com.ztomic.ircbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ztomic.ircbot.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

	@Query("select q from Question q where UPPER(q.language) = UPPER(?1)")
	public List<Question> findByLanguage(String language);
	
}
