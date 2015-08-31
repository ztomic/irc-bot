package com.ztomic.ircbot.model;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ztomic.ircbot.model.converter.StringListConverter;

@Table(name = "questions")
@Entity
public class Question {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "question_id", unique = true, nullable = false, updatable = false)
	private long id;

	@Column
	private String language;

	@Column(name = "theme")
	private String theme;

	@Column(name = "question", length = 1000, columnDefinition = "varchar(1000) not null")
	private String question;

	@Column(name = "answers")
	@Convert(converter = StringListConverter.class)
	private StringList answers;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public StringList getAnswers() {
		return answers;
	}
	
	public void setAnswers(StringList answers) {
		this.answers = answers;
	}

	@Override
	public String toString() {
		return String.format("Question [id=%s, language=%s, theme=%s, question=%s, answers=%s", id, language, theme, question, getAnswers());
	}

}
