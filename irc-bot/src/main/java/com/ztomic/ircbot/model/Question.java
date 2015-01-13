package com.ztomic.ircbot.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.StringUtils;

@NamedQueries({ @NamedQuery(name = "Question.selectAll", query = "SELECT q from Question q where q.language = :language or q.language is null", hints = { @QueryHint(name = "org.hibernate.cacheRegion", value = "Query.Question.selectAll"), @QueryHint(name = "org.hibernate.cacheable", value = "true") }), })
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

	@Column
	private String answer1;

	@Column
	private String answer2;

	@Column
	private String answer3;

	@Column
	private String answer4;

	@Column
	private String answer5;

	@Column
	private String answer6;

	@Column
	private String answer7;

	@Column
	private String answer8;

	@Column
	private String answer9;

	@Column
	private String answer10;

	@Column
	private String answer11;

	@Column
	private String answer12;

	@Column
	private String answer13;

	@Column
	private String answer14;

	@Column
	private String answer15;

	@Column
	private String answer16;

	@Column
	private String answer17;

	@Column
	private String answer18;

	@Column
	private String answer19;

	@Column
	private String answer20;

	@Transient
	private List<String> answers;

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

	public List<String> getAnswers() {
		if (answers != null)
			return answers;
		final List<String> answers = new ArrayList<String>();
		ReflectionUtils.doWithFields(getClass(), new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (field.getName().startsWith("answer")) {
					ReflectionUtils.makeAccessible(field);
					try {
						String value = (String) field.get(Question.this);
						if (StringUtils.hasText(value)) {
							answers.add((String) value);
						}
					} catch (IllegalArgumentException e) {
						//
					} catch (IllegalAccessException e) {
						//
					}
				}
			}
		});
		this.answers = answers;
		return answers;
	}

	@Override
	public String toString() {
		return String.format("Question [id=%s, language=%s, theme=%s, question=%s, answers=%s", id, language, theme, question, getAnswers());
	}

}
