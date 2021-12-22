package com.webapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class QuickTest {

	public static void main(String[] args) throws IOException {
		String path = "src/main/resources";

		File file = new File(path);
		String absolutePath = file.getAbsolutePath();
		for(File fi: file.listFiles()) {
			System.out.println(fi.getAbsolutePath());
		}
		File fi = new File(path + "/test");
		if (!fi.exists())
			Files.createDirectories(fi.toPath());
		fi = new File(file.getAbsolutePath() + "/test/test1.txt");
		if (!fi.exists())
			Files.createFile(fi.toPath());
		System.out.println(absolutePath);
	}

}
