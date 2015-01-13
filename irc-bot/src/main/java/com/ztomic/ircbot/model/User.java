package com.ztomic.ircbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return String.format("User [id=%s, level=%s, nick=%s, ident=%s, hostname=%s, server=%s, password=%s]", id, level, nick, ident, hostname, server, password);
	}

}