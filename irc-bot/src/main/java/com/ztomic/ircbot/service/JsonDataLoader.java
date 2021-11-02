package com.ztomic.ircbot.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.core.io.Resource;

public class JsonDataLoader {

	private Resource file;

	public Resource getFile() {
		return file;
	}

	public void setFile(Resource file) {
		this.file = file;
	}

	private ObjectMapper getObjectMapper() {
		return new ObjectMapper()
				.enable(JsonParser.Feature.ALLOW_COMMENTS)
				.enable(SerializationFeature.INDENT_OUTPUT);
	}

	<T> List<T> readFile(Class<T> javaType) throws IOException {
		ObjectMapper objectMapper = getObjectMapper();
		return objectMapper.readValue(file.getFile(), objectMapper.getTypeFactory().constructCollectionType(List.class, objectMapper.getTypeFactory().constructType(javaType)));
	}

}
