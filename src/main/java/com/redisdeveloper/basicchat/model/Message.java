package com.redisdeveloper.basicchat.model;

public class Message {
	private String from;
	private String to;
	private long date;
	private String message;
	private String roomId;

	public Message(String from, int date, String message, String roomId) {
		this.from = from;
		this.date = date;
		this.message = message;
		this.roomId = roomId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public long getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setDate(long date) {
		this.date = date;
	}

}
