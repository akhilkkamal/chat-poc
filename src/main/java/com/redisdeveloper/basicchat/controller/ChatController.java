package com.redisdeveloper.basicchat.controller;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redisdeveloper.basicchat.model.ChatControllerMessage;
import com.redisdeveloper.basicchat.model.Message;
import com.redisdeveloper.basicchat.model.MessageType;
import com.redisdeveloper.basicchat.model.PubSubMessage;
import com.redisdeveloper.basicchat.repository.RoomsRepository;
import com.redisdeveloper.basicchat.repository.UsersRepository;
import com.redisdeveloper.basicchat.service.RedisMessageSubscriber;

@RestController
@RequestMapping("/chat")
public class ChatController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private RoomsRepository roomsRepository;

	@Autowired
	ChannelTopic topic;

	@Autowired
	MessageListenerAdapter messageListener;

	@GetMapping("/stream")
	public SseEmitter connectToStream(@RequestParam String userName) {

		System.out.println("Inside stream");

		AtomicBoolean isComplete = new AtomicBoolean(false);

		// Lets set 10 minute as default timeout for a chat session
		SseEmitter emitter = new SseEmitter(60 * 10 * 1000l);

		Function<String, Integer> handler = (String message) -> {

			JsonObject object = JsonParser.parseString(message).getAsJsonObject();
			String type = object.get("type").getAsString();
			JsonObject data = object.get("data").getAsJsonObject();
			if (!type.equals("message")) {
				return 1;
			} else if (type.equals("message")) {
				String fromAddress = data.get("from").getAsString();
				String toAddress = data.get("to").getAsString();

				if (!(toAddress.equals(userName))) {
					System.out.println(
							String.format("This message is between user %s and %s. Ignoreing this message for user %s",
									fromAddress, toAddress, userName));
					return 1;
				}
			}

			SseEmitter.SseEventBuilder event = SseEmitter.event().data(message);
			try {
				emitter.send(event);
			} catch (IOException e) {
				// This may occur when the client was disconnected.
				return 1;
			}
			return 0;
		};

		RedisMessageSubscriber redisMessageSubscriber = (RedisMessageSubscriber) messageListener.getDelegate();
		redisMessageSubscriber.attach(handler);

		Runnable onDetach = () -> {
			redisMessageSubscriber.detach(handler);
			if (!isComplete.get()) {
				isComplete.set(true);
				emitter.complete();
			}
			System.out.println("onDetach");
		};

		Runnable onTimeout = () -> {
			redisMessageSubscriber.detach(handler);
			if (!isComplete.get()) {
				isComplete.set(true);
				emitter.complete();
			}
			System.out.println("onTimeout");
		};

		Runnable onError = () -> {
			redisMessageSubscriber.detach(handler);
			if (!isComplete.get()) {
				isComplete.set(true);
				emitter.complete();
			}
			System.out.println("onError");
		};

		emitter.onCompletion(onDetach);
		emitter.onError((err) -> onError.run());
		emitter.onTimeout(onTimeout);

		return emitter;
	}

	/**
	 * Receive incoming messages from the client...
	 */

	@PostMapping(path = "/emit", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> sendMessage(@RequestBody ChatControllerMessage chatMessage) {
		String serializedMessage = null;

		LOGGER.info("Received message: " + chatMessage.toString());

		if (chatMessage.getMessageType() == MessageType.MESSAGE) {
			serializedMessage = handleRegularMessageCase(chatMessage);
		}
		// Finally, send the serialized json to Redis.
		roomsRepository.sendMessageToRedis(topic.getTopic(), serializedMessage);

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	private String handleRegularMessageCase(ChatControllerMessage chatMessage) {
		Gson gson = new Gson();
		// We've received a message from user. It's necessary to deserialize it first.
		Message message = chatMessage.getMessage();
		message.setDate(System.currentTimeMillis());

		// Add the user who sent the message to online list.
		usersRepository.addUserToOnlineList(message.getFrom());

		// Add room to the user room list
		usersRepository.addRoomToUserRooms(message.getFrom(), message.getRoomId());

		// Add room to the user room list
		usersRepository.addRoomToUserRooms(message.getTo(), message.getRoomId());

		// Write the message to DB.
		roomsRepository.saveMessage(message);
		// Finally create the serialized output which would go to pub/sub
		return gson.toJson(new PubSubMessage<>(chatMessage.getMessageType().value(), message));
	}

}
