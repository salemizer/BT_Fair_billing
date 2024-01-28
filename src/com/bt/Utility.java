package com.bt;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utility {

	public static boolean StringNotContainsSpecialChar(String str) {
		
		Pattern p = Pattern.compile("[^a-z0-9\\\\\\\\u0020]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		return m.find();
	}

	public static LocalTime parseStringToLocalTime(String str, String pattern) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return LocalTime.parse(str, formatter);
	}

}
