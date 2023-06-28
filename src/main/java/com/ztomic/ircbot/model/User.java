package com.ztomic.ircbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

	public enum Level {
		NEWBIE, REGISTERED, MASTER;

		public static Level valueOf(String name, Level def) {
			for (Level l : values()) {
				if (l.name().equalsIgnoreCase(name)) {
					return l;
				}
			}
			return def;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private long id;

	@Column
	private String nick;

	@Column
	private String server;

	@Column
	private String ident;

	@Column
	private String hostname;

	@Column
	private String password;

	@Enumerated(EnumType.STRING)
	@Column
	private Level level = Level.NEWBIE;

}