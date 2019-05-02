package com.ztomic.ircbot.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ztomic.ircbot.model.converter.StringListConverter;
import lombok.Data;
import lombok.ToString;

@Table(name = "questions")
@Entity
@Data
@ToString(exclude = "answers")
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
	private List<String> answers;

}
