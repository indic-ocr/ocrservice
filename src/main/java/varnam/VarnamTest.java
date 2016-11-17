package varnam;

import java.util.List;

public class VarnamTest {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		Varnam varnam = new Varnam("/usr/local/share/varnam/vst/ta.vst");
		varnam.enableSuggestions("learnings.varnam.ta");
		List<Word> words = varnam.transliterate("balaji bakers");
		for (Word word : words) {
		    System.out.println(word.getConfidence() + " - " + word.getText());
		    
		    System.out.println( varnam.reverseTransliterate(word.getText()));
			
		}
		
		
	}

}
