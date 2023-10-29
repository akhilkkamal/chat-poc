package com.redisdeveloper.basicchat.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.redisdeveloper.basicchat.model.Message;
import com.redisdeveloper.basicchat.model.Room;
import com.redisdeveloper.basicchat.repository.RoomsRepository;

@RestController
@RequestMapping("/rooms")
public class RoomsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoomsController.class);

	@Autowired
	private RoomsRepository roomsRepository;

	/**
	 * Get rooms for specific user id.
	 */
	@GetMapping(value = "user/{userName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Room>> getRooms(@PathVariable String userName) {
		Set<String> roomNames = roomsRepository.getUserRooms(userName);
		if (roomNames == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		List<Room> rooms = new ArrayList<>();

		for (String roomName : roomNames) {
			boolean roomExists = roomsRepository.isRoomExists(roomName);
			if (roomExists) {
				rooms.add(new Room(roomName));
			}
		}
		return new ResponseEntity<>(rooms, HttpStatus.OK);
	}

	/**
	 * Get Messages.
	 */
	@GetMapping(value = "messages/{roomName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Message>> getMessages(@PathVariable String roomName, @RequestParam int offset,
			@RequestParam int size) {
		boolean roomExists = roomsRepository.isRoomExists(roomName);
		List<Message> messages = new ArrayList<>();
		if (roomExists) {
			Set<String> values = roomsRepository.getMessages(roomName, offset, size);
			for (String value : values) {
				messages.add(deserialize(value));
			}
		}
		return new ResponseEntity<>(messages, HttpStatus.OK);
	}

	private Message deserialize(String value) {
		Gson gson = new Gson();
		try {
			return gson.fromJson(value, Message.class);
		} catch (Exception e) {
			LOGGER.error(String.format("Couldn't deserialize json: %s", value), e);
		}
		return null;
	}
}
