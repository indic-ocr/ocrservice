package in.rkvsraman.indicocr.webservice;

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;

public class OCRPostProcessor {

	static HashMap<String, TreeMap<String, String>> postProcessMap = new HashMap<String, TreeMap<String, String>>();

	public static void main(String args[]) throws Exception {

		if (args.length != 2) {
			System.out.println("Usage: java in.rkvsraman.indicocr.webservice.OCRPostProcessor fileName lang");
			System.exit(1);

		}

		File f = new File(args[0]);

		List<String> list = IOUtils.readLines(new FileReader(f));// ,
																	// Charset.forName("UTF-16"));

		StringBuffer sb = new StringBuffer();
		for (String s : list) {
			sb.append(getProcessedString(args[1], s));
		}

		System.out.println(sb.toString());
	}

	public static String getProcessedString(String lang, String text) {

		TreeMap<String, String> map = postProcessMap.get(lang);

		if (map == null) {

			InputStream ins = OCRPostProcessor.class
					.getResourceAsStream("/tesseractpostprocessor/" + lang + ".properties");

			if (ins != null) {

				Properties p = new Properties();
				try {
					p.load(ins);
					map = new TreeMap<String, String>();
					for (String s : p.stringPropertyNames()) {

						map.put(s, p.getProperty(s));
					}

					postProcessMap.put(lang, map);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return text;
				}
			} else {
				System.out.println("No language resource found for post processing for " + lang);
				return text;
			}

		}
		if (map == null) {
			return text;
		}
		if (text != null) {
			String replacedText = new String(text);
			for (String s : map.descendingKeySet()) {

				// System.out.println(s);
				replacedText = replacedText.replaceAll(s, map.get(s));
			}

			return replacedText;
		}
		return null;
	}

}
