package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class FixGameNames {

	public static void main(String[] args) throws Exception {

		String inputFileName = "../gfx/games.list";
		String outputFileName = "../gfx/games2.list";

		BufferedReader is = new BufferedReader(new FileReader(inputFileName));

		FileWriter fos = new FileWriter(outputFileName, false);

		while (true) {
			String line = is.readLine();
			if (line == null) {
				break;
			}

			if (line.charAt(0) == 'n') {
				line = line.substring(2);
				line = line.replace('_', ' ');
				line = line.toUpperCase();
				line = "n " + line;
			}

			fos.write(line);
			fos.write('\n');
		}

		is.close();
		fos.close();

	}

}
