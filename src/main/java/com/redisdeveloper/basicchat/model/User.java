package com.redisdeveloper.basicchat.model;

public class User {
	private String username;
	private boolean isOnline;

	public User(String username, boolean isOnline) {
		this.username = username;
		this.isOnline = isOnline;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
