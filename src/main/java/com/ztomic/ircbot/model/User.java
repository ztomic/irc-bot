package com.ztomic.ircbot.model;

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