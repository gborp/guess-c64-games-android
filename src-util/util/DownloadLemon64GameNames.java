package util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class DownloadLemon64GameNames {

	public static void main(String[] args) throws Exception {

		String outputFile = "../gfx/games.list";
		String outputFileDir = "../gfx";

		Properties prop = new Properties();
		prop.load(new BufferedInputStream(new FileInputStream("games.list")));

	}

}
