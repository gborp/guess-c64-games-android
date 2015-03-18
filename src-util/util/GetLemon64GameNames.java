package util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class GetLemon64GameNames {

	public static void main(String[] args) throws Exception {

		String outputFileName = "../gfx/games.list";

		int i = 0;
		int sameCounter = 0;

		boolean firstFetch = true;
		String firstFetchName = null;
		String detailLinkRoot = "http://www.lemon64.com/games/details.php?ID=";
		HashMap<String, InfoSlot> map = new HashMap<String, InfoSlot>();
		while (true) {

			if (Math.random() > 0.9) {
				Thread.sleep(100);
			}

			ArrayList<String> lstGfx = new ArrayList<String>();

			String urlName = detailLinkRoot + i;
			URL url = new URL(urlName);
			InputStream is = url.openStream();
			DataInputStream dis = new DataInputStream(new BufferedInputStream(
					is));

			String line;

			while ((line = dis.readLine()) != null) {

				int startPos = line
						.indexOf("http://www.lemon64.com/games/screenshots/full");
				if (startPos != -1) {
					int endPos = line.indexOf(".gif", startPos);
					String gfxUrl = line.substring(startPos,
							endPos + ".gif".length());

					String gfxUrlRoot = gfxUrl.substring(0, gfxUrl.length()
							- "##.gif".length());

					for (int subGfxIndex = 1; subGfxIndex < 10; subGfxIndex++) {
						String fullGfxUrl = gfxUrlRoot + "0" + subGfxIndex
								+ ".gif";

						URL u = new URL(fullGfxUrl);
						HttpURLConnection huc = (HttpURLConnection) u
								.openConnection();
						huc.setRequestMethod("HEAD"); // OR huc.setRequestMethod
														// ("HEAD");
						huc.connect();
						int code = huc.getResponseCode();

						if (code == 200) {
							lstGfx.add(fullGfxUrl);
						} else {
							break;
						}
					}

					break;
				}

			}

			String name = lstGfx.get(0);
			name = name.substring(name.lastIndexOf('/') + 1, name.length() - 7);

			if (firstFetch) {
				firstFetch = false;
				firstFetchName = name;
			} else {
				firstFetch = true;

				if (!firstFetchName.equals(name)) {
					System.out.println("id " + i + " random");
					i++;
					continue;
				}

				System.out.println("id " + i + " good");
				i++;

				if (map.containsKey(name)) {
					sameCounter++;
					// System.out.println("same counter " + sameCounter +
					// "name: "
					// + name);
					if (sameCounter == 400000) {
						break;
					}
				} else {
					InfoSlot slot = new InfoSlot();
					slot.name = name;
					slot.url = urlName;
					slot.lstGfx = lstGfx;

					map.put(name, slot);

					Writer fos = new BufferedWriter(new FileWriter(
							outputFileName, true));

					fos.write("n " + slot.name + "\n");
					fos.write("u " + slot.url + "\n");
					for (String li : slot.lstGfx) {
						fos.write("g "
								+ li.substring("http://www.lemon64.com/games/screenshots/full"
										.length()) + "\n");
					}
					fos.write("-\n");

					fos.close();

					int mapSize = map.size();
					if ((mapSize % 1000) == 0) {
						System.out.println("Game count " + mapSize);
					}
					if (mapSize > 14373) {
						break;
					}
				}
			}
		}

		// ArrayList<InfoSlot> lst = new ArrayList<InfoSlot>(map.values());
		// Collections.sort(lst, new Comparator<InfoSlot>() {
		//
		// public int compare(InfoSlot o1, InfoSlot o2) {
		// return o1.name.compareTo(o2.name);
		// }
		// });

		// FileWriter fos = new FileWriter(outputFileName, true);
		//
		// for (InfoSlot slot : lst) {
		// fos.write("n " + slot.name + "\n");
		// fos.write("u " + slot.url + "\n");
		// for (String li : slot.lstGfx) {
		// fos.write("g "
		// + li.substring("http://www.lemon64.com/games/screenshots/full"
		// .length()) + "\n");
		// }
		// fos.write("-\n");
		// }
		//
		// fos.close();

	}

	public static class InfoSlot {

		public String url;
		public String name;
		public ArrayList<String> lstGfx;

	}
}
