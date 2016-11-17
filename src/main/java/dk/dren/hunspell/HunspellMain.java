package dk.dren.hunspell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

/**
 * Simple testing and native build utility class, not useful in applications.
 *
 * The Hunspell java bindings are licensed under the same terms as Hunspell
 * itself (GPL/LGPL/MPL tri-license), see the file COPYING.txt in the root of
 * the distribution for the exact terms.
 *
 * @author Flemming Frandsen (flfr at stibo dot com)
 * @author Hartmut Goebel (h dot goebel at crazy-compilers dot com)
 *
 *         Usage: java -Droot=/usr/share/dicts -Dlang=de_DE HunspellMain
 */

public class HunspellMain {

	private static void println(String msg) {
		System.out.println(msg);
	}

	private static void print(String msg) {
		System.out.print(msg);
	}

	public static void main(String[] args) throws Exception {
		
		if(args.length != 2){
			println("Usage: java dk.dren.hunspell.HunspellMain lang fileName");
			System.exit(1);
		}
		
		

		System.err.println("Loading Hunspell");
		String dir = "/usr/share/hunspell";
		if (System.getProperties().containsKey("root")) {
			dir = System.getProperty("root");
		}

		String language = args[0];
		if (System.getProperties().containsKey("lang")) {
			language = System.getProperty("lang");
		}

		Hunspell.Dictionary d = Hunspell.getInstance().getDictionary(dir + "/" + language);
		System.err.println("Hunspell library and dictionary loaded");

		String words[] = { "Test", "Impontont", "guest", "ombudsmandshat", "ombudsman", "ymerfest",
				"g0r\u00f8ftegraver", "h\u00e6ngeplante", "garageport", "postbil", "huskop", "arne", "pladderballe",
				"Doctor", "Leo", "Lummerkrog", "Barnevognsbrand", "barnehovedbekl\u00e6dning", "ymer", "drys",
				"ymerdrys", "\u00e6sel", "m\u00e6lk", "\u00e6selm\u00e6lk", "Brotbacken", "Pausbacken", "pausbackig",
				"Backenknochenbruch", "Donnerdampfschifffahrt", "Donnerdampfschifffahrtsgesellschaftskapit\u00e4n",
				"Messer", "Schleifer", "Messerschleifer", "muss", "mu\u00df" };
		
		BufferedReader br = new BufferedReader(new FileReader(args[1]));
		String s = new String();
		StringBuffer sb = new StringBuffer();
		while ((s = br.readLine()) != null){
			sb.append(s + " ");
		}
		
		words = sb.toString().trim().split(" ");

		for (int i = 0; i < words.length; i++) {

			
			String word = words[i];
			if(word.length() == 0)
				continue;
			if (d.misspelled(word)) {
				List<String> suggestions = d.suggest(word);
				print("misspelled: " + word);
				if (suggestions.isEmpty()) {
					print("\tNo suggestions.");
				} else {
					print("\tTry:");
					for (String s1 : suggestions) {
						print(" " + s1);
					}
				}
				println("");
			} else {
				println("ok: " + word);
			}
		}

	}
}
