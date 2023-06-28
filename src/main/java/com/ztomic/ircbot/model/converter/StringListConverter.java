package com.ztomic.ircbot.model.converter;

import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.util.StringUtils;

/**
 * Converter for list of strings delimited with new line
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String>{

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		if (attribute != null) {
			return String.join("\n", attribute);
		}
		return "";
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		List<String> sl = new LinkedList<>();
		if (dbData != null) {
			dbData.lines()
					.filter(StringUtils::hasText)
					.forEach(sl::add);
		}
		return sl;
	}

}
