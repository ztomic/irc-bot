package com.ztomic.ircbot.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztomic.ircbot.model.Question;
import com.ztomic.ircbot.model.converter.StringListConverter;
import com.ztomic.ircbot.repository.QuestionRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@ConfigurationProperties("questions.loader")
@ConditionalOnProperty(prefix = "questions.loader", value = "file")
public class QuestionLoader implements CommandLineRunner {

	private final QuestionRepository questionRepository;

	private Resource file;

	public QuestionLoader(QuestionRepository questionRepository) {
		this.questionRepository = questionRepository;
	}

	public Resource getFile() {
		return file;
	}

	public void setFile(Resource file) {
		this.file = file;
	}

	@Override
	public void run(String... args) throws Exception {
		long count = questionRepository.count();
		log.info("Total questions: {}", count);
		if (count == 0) {
			ObjectMapper objectMapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS);
			List<QuestionResource> questions = objectMapper.readValue(file.getFile(), objectMapper.getTypeFactory().constructCollectionType(List.class, objectMapper.getTypeFactory().constructType(QuestionResource.class)));
			Stream<Question> questionStream = questions.stream()
					.map(r -> {
						Question q = new Question();
						q.setLanguage(r.getLanguage());
						q.setTheme(r.getTheme());
						q.setQuestion(r.getQuestion());
						q.setAnswers(new StringListConverter().convertToEntityAttribute(r.getAnswers()));
						return q;
					});
			questionRepository.saveAll(questionStream.collect(Collectors.toList()));
			log.info("Imported questions: {}", questionRepository.count());
		}
	}

	@Data
	public static class QuestionResource {
		private String language;
		private String theme;
		private String question;
		private String answers;
	}

}
