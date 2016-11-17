/**
 * 
 */
package varnam;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * 
 *
 */
public final class Varnam {
	
	private final String vstfile;
	private final Pointer handle;

	public Varnam(String vstfile) throws VarnamException {
		this.vstfile = vstfile;
		PointerByReference varnamHandle = new PointerByReference();
		PointerByReference errorMessage = new PointerByReference();
		int status = VarnamLibrary.INSTANCE.varnam_init(vstfile, varnamHandle, errorMessage);
		if (status != 0) {
			throw new VarnamException("Error initializing varnam." + errorMessage.getValue().getString(0));
		}
		
		this.handle = varnamHandle.getValue();
		VarnamLibrary.INSTANCE.varnam_config(handle, 102, "/home/raman/ocr/varnam/libvarnam/schemes/ml");
	}
	
	public void enableSuggestions(String suggestionsFile) {
		VarnamLibrary.INSTANCE.varnam_config(handle, 102, suggestionsFile);
	}
	
	public List<Word> transliterate(String textToTransliterate) throws VarnamException {
		PointerByReference output = new PointerByReference();
		VarnamLibrary library = VarnamLibrary.INSTANCE;
		int status = library.varnam_transliterate(handle, textToTransliterate, output);
		if (status != 0) {
			throw new VarnamException(library.varnam_get_last_error(handle));
		}
		
		ArrayList<Word> words = new ArrayList<Word>();
		Pointer result = output.getValue();
		int totalWords = library.varray_length(result);
		for (int i = 0; i < totalWords; i++) {
			Pointer item = library.varray_get(result, i);
			words.add(new Word(item));
		}
		
		return words;
	}
	
	public String reverseTransliterate(String textToTransliterate) throws VarnamException {
		PointerByReference output = new PointerByReference();
		VarnamLibrary library = VarnamLibrary.INSTANCE;
		int status = library.varnam_reverse_transliterate(handle, textToTransliterate, output);
		if (status != 0) {
			throw new VarnamException(library.varnam_get_last_error(handle));
		}
		
		Pointer p = output.getValue();
		return  p.getString(0);
	}
	
	public void learn(String word) throws VarnamException {
		int status = VarnamLibrary.INSTANCE.varnam_learn(handle, word);
		if (status != 0) {
			throw new VarnamException(VarnamLibrary.INSTANCE.varnam_get_last_error(handle));
		}
	}

	public String getVstfile() {
		return vstfile;
	}

}
