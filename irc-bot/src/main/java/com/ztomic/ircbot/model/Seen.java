package com.ztomic.ircbot.model;

import java.util.Date;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "seen")
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

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date time;

	@Enumerated(EnumType.STRING)
	@Column
	private EventType type;

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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, String> getDetail() {
		return detail;
	}

	public void setDetail(HashMap<String, String> detail) {
		this.detail = detail;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public void addDetail(String key, String value) {
		if (detail == null) {
			detail = new HashMap<String, String>();
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