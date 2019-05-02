package com.ztomic.ircbot.model.converter;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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
			Pattern.compile("\n")
					.splitAsStream(dbData)
					.filter(StringUtils::hasText)
					.forEach(sl::add);
		}
		return sl;
	}

}
