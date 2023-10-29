package com.redisdeveloper.basicchat.model;

public class ChatControllerMessage {
	private MessageType messageType;
	private Message message;

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

}
