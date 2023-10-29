package com.redisdeveloper.basicchat.repository;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.redisdeveloper.basicchat.model.User;

@Repository
public class UsersRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(UsersRepository.class);

	private static final String ONLINE_USERS_KEY = "online_users";
	private static final String USER_KEY = "user:%s";
	private static final String USER_ROOM_KEY = "user:%s:rooms";

	@Autowired
	private StringRedisTemplate redisTemplate;

	public User getUserOnlineStatus(String userName) {

		boolean isOnline = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, String.valueOf(userName));
		return new User(userName, isOnline);
	}

	public Set<String> getOnlineUsersIds() {
		Set<String> onlineIds = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
		if (onlineIds == null) {
			LOGGER.info("No online users found");
			return null;
		}
		return onlineIds.stream().collect(Collectors.toSet());
	}

	public boolean isUserExists(String username) {
		return redisTemplate.hasKey(String.format(USER_KEY, username));
	}

	public User getUserDetails(String username) {
		if (!isUserExists(username)) {
			return null;
		}
		boolean isOnline = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, String.valueOf(username));
		return new User(username, isOnline);
	}

	public void addRoomToUserRooms(String userName, String roomName) {
		redisTemplate.opsForSet().add(String.format(USER_ROOM_KEY, userName), roomName);
	}

	public void addUserToOnlineList(String userName) {
		redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userName);
	}

	public void removeUserFromOnlineList(String userName) {
		redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userName);
	}

}
