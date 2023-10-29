package com.redisdeveloper.basicchat.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redisdeveloper.basicchat.model.User;
import com.redisdeveloper.basicchat.repository.UsersRepository;

@RestController
@RequestMapping("/users")
public class UsersController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	private UsersRepository usersRepository;

	/**
	 * The request the client sends to check online status of specified user
	 */
	@GetMapping(value = "onlineStatus/{userNames}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, User>> getOnlineStatus(
			@RequestParam(value = "userNames") String userNamesString) {
		Set<String> userNames = parseIds(userNamesString);

		Map<String, User> usersMap = new HashMap<>();

		for (String userName : userNames) {
			User user = usersRepository.getUserOnlineStatus(userName);
			if (user == null) {
				LOGGER.debug("User not found by id: " + userName);
				return new ResponseEntity<>(new HashMap<>(), HttpStatus.BAD_REQUEST);
			}
			usersMap.put(String.valueOf(user.getUsername()), user);
		}
		return new ResponseEntity<>(usersMap, HttpStatus.OK);
	}

	private Set<String> parseIds(String idsString) {
		return Arrays.stream(idsString.split(","))
				// .map(Integer::parseInt)
				.collect(Collectors.toSet());
	}

}
