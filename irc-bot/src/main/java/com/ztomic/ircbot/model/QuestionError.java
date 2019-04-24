package com.ztomic.ircbot.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "question_errors")
@Entity
@Data
public class QuestionError {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "error_id", unique = true, nullable = false, updatable = false)
	private long id;

	@Column(name = "question_id")
	private long questionId;

	@Column(name = "user_id")
	private long userId;

	@Column
	@Lob
	private String reason;

	@Column
	private boolean fixed;

	@Column(name = "time_reported")
	private LocalDateTime timeReported;

	@Column(name = "time_fixed")
	private LocalDateTime timeFixed;

}
