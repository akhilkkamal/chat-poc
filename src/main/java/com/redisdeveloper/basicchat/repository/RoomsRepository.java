package com.redisdeveloper.basicchat.repository;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
import com.redisdeveloper.basicchat.model.Message;

@Repository
public class RoomsRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoomsRepository.class);

	@Autowired
	private StringRedisTemplate redisTemplate;

	private static final String USER_ROOMS_KEY = "user:%s:rooms";
	public static final String ROOM_KEY = "room:%s";

	public Set<String> getUserRooms(String userName) {
		String userRoomsKey = String.format(USER_ROOMS_KEY, userName);
		Set<String> roomIds = redisTemplate.opsForSet().members(userRoomsKey);
		LOGGER.debug("Received roomIds by userId: " + userName);
		return roomIds;
	}

	public boolean isRoomExists(String roomId) {
		return redisTemplate.hasKey(String.format(ROOM_KEY, roomId));
	}

	public Set<String> getMessages(String roomId, int offset, int size) {
		String roomNameKey = String.format(ROOM_KEY, roomId);
		Set<String> messages = redisTemplate.opsForZSet().reverseRange(roomNameKey, offset, offset + size);
		LOGGER.debug(String.format("Received messages by roomId:%s, offset:%s, size:%s ", roomId, offset, size));
		return messages;
	}

	public void sendMessageToRedis(String topic, String serializedMessage) {
		LOGGER.debug(String.format("Saving message to Redis: topic:%s, message:%s ", topic, serializedMessage));
		redisTemplate.convertAndSend(topic, serializedMessage);
	}

	public void saveMessage(Message message) {
		Gson gson = new Gson();
		String roomKey = String.format(ROOM_KEY, message.getRoomId());
		redisTemplate.opsForZSet().add(roomKey, gson.toJson(message), message.getDate());
	}
}
