package org.beryx.textio;

import java.util.HashMap;
import java.util.Map;

public class Translate {
	private static final Map<String, String> trMap = new HashMap<>();

	protected static String tr(String origText) {
		return trMap.getOrDefault(origText, origText);
	}

	public static void addTranslation(String origText, String trText) {
		trMap.put(origText, trText);
	}

	public static void removeTranslation(String origText) {
		trMap.remove(origText);
	}

	public static void clearTranslation() {
		trMap.clear();
	}
}
