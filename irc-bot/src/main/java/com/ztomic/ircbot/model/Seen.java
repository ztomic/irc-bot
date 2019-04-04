package com.ztomic.ircbot.model;

import java.time.LocalDateTime;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "seen")
@Data
public class Seen {

	public enum EventType {
		Quit, Part, Kick, Nick, Join
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false, updatable = false)
	private long id;

	@Column
	private String nick;

	@Column
	private String server;

	@Column
	private String channel;

	@Column
	private String ident;

	@Column
	private String host;

	@Column
	private String name;

	@Column
	private HashMap<String, String> detail;

	@Column
	private LocalDateTime time;

	@Enumerated(EnumType.STRING)
	@Column
	private EventType type;

	public void addDetail(String key, String value) {
		if (detail == null) {
			detail = new HashMap<>();
		}
		detail.put(key, value);
	}

	public String getDetail(String key) {
		if (detail == null) {
			return null;
		}
		return detail.get(key);
	}

}