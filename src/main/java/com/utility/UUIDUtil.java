package com.utility;

import java.util.UUID;

public class UUIDUtil {
	public static String getUUID() {
		UUID uuid = UUID.randomUUID(); //Generates random UUID  
		return uuid.toString();
	}
}
