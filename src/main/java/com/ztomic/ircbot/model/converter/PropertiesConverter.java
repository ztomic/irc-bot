package com.ztomic.ircbot.model.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PropertiesConverter implements AttributeConverter<Properties, String> {

	@Override
	public String convertToDatabaseColumn(Properties attribute) {
		if (attribute != null && !attribute.isEmpty()) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				attribute.store(os, null);
				return os.toString().lines().skip(1).collect(Collectors.joining("\n"));
			} catch (IOException e) {
				// swallow
			}
		}
		return null;
	}

	@Override
	public Properties convertToEntityAttribute(String dbData) {
		Properties properties = new Properties();
		if (dbData != null) {
			try {
				properties.load(new ByteArrayInputStream(dbData.getBytes(StandardCharsets.UTF_8)));
			} catch (IOException e) {
				//
			}
		}
		return properties;
	}

}
