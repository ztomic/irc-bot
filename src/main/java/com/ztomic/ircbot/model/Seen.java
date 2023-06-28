package com.ztomic.ircbot.model;

import java.time.LocalDateTime;
import java.util.Properties;

import com.ztomic.ircbot.model.converter.PropertiesConverter;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

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

	@Basic
	@Lob
	@Column
	private String ident;

	@Column
	private String host;

	@Basic
	@Lob
	@Column
	private String name;

	@Column
	@Convert(converter = PropertiesConverter.class)
	private Properties detail;

	@Column
	private LocalDateTime time;

	@Enumerated(EnumType.STRING)
	@Column
	private EventType type;

	public void addDetail(String key, String value) {
		if (detail == null) {
			detail = new Properties();
		}
		detail.put(key, value);
	}

	public String getDetail(String key) {
		if (detail == null) {
			return null;
		}
		return detail.getProperty(key);
	}

}