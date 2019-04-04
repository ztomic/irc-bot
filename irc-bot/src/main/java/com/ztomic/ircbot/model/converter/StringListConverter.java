package com.ztomic.ircbot.model.converter;

import java.util.Arrays;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.util.StringUtils;

import com.ztomic.ircbot.model.StringList;

/**
 * Converter for list of strings delimited with new line
 */
@Converter
public class StringListConverter implements AttributeConverter<StringList, String>{

	@Override
	public String convertToDatabaseColumn(StringList attribute) {
		if (attribute != null) {
			return String.join("\n", attribute);
		}
		return "";
	}

	@Override
	public StringList convertToEntityAttribute(String dbData) {
		StringList sl = new StringList();
		if (dbData != null) {
			sl.addAll(Arrays.asList(dbData.split("\n")));
			sl.removeIf(StringUtils::isEmpty);
		}
		return sl;
	}

}
