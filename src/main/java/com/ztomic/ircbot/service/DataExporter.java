package com.ztomic.ircbot.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztomic.ircbot.repository.PlayerRepository;
import com.ztomic.ircbot.repository.QuestionRepository;
import com.ztomic.ircbot.repository.SeenRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("data.export")
public class DataExporter implements CommandLineRunner {

	private final PlayerRepository playerRepository;
	private final SeenRepository seenRepository;
	private final QuestionRepository questionRepository;
	private final ObjectMapper objectMapper;

	public DataExporter(PlayerRepository playerRepository, SeenRepository seenRepository, QuestionRepository questionRepository, ObjectMapper objectMapper) {
		this.playerRepository = playerRepository;
		this.seenRepository = seenRepository;
		this.questionRepository = questionRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public void run(String... args) throws Exception {
		Path file = Files.createFile(Paths.get("player-data.json"));
		objectMapper.writeValue(file.toFile(), playerRepository.findAll(Sort.by("id")));

		file = Files.createFile(Paths.get("question-data.json"));
		objectMapper.writeValue(file.toFile(), questionRepository.findAll(Sort.by("id")));

		file = Files.createFile(Paths.get("seen-data.json"));
		objectMapper.writeValue(file.toFile(), seenRepository.findAll(Sort.by("id")));
	}
}
