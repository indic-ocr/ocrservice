package in.rkvsraman.indicocr.webservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import varnam.Varnam;
import varnam.VarnamException;
import varnam.Word;

public class VarnamUtils {

	private static HashMap<String, Varnam> varnamMap = null;

	public static String getEnglishString(String s, String sourcelang) {
		// TODO Auto-generated method stub
		if (varnamMap == null)
			populateMaps();

		Varnam varnam = varnamMap.get(sourcelang);
		if (varnam != null) {
			try {
				return varnam.reverseTransliterate(s);
			} catch (VarnamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	private static void populateMaps() {
		Properties langcodes = new Properties();
		varnamMap = new HashMap<String, Varnam>();

		try {
			langcodes.load(VarnamUtils.class.getResourceAsStream("/tesseractpostprocessor/langcode.properties"));

			for (String s : langcodes.stringPropertyNames()) {

				Varnam locVarnam = new Varnam("/usr/local/share/varnam/vst/" + langcodes.getProperty(s) + ".vst");
				locVarnam.enableSuggestions("learnings.varnam" + langcodes.getProperty(s));
				varnamMap.put(s, locVarnam);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String transliterate(String toEnglish, String tolang) {
		// TODO Auto-generated method stub
		if (varnamMap == null)
			populateMaps();

		Varnam varnam = varnamMap.get(tolang);
		if (varnam != null) {
			try {
				List<Word> words = varnam.transliterate(toEnglish);
				int conf = 0;
				String tWord = "";
				for (Word word : words) {
					if(word.getConfidence() > conf){
						conf = word.getConfidence();
						tWord = word.getText();
					}
					
				}
				return tWord;
			} catch (VarnamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return toEnglish;
	}

}
