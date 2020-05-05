package net.briac.omegat.plugin.voikko;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.omegat.util.StaticUtils;
import org.puimula.libvoikko.Dictionary;
import org.puimula.libvoikko.Voikko;
import org.puimula.libvoikko.VoikkoException;

public class VoikkoInstance {

    private static final Map<String, Voikko> voikko = new HashMap<>();

    protected static final File VOIKKO_DIRECTORY = new File(StaticUtils.getConfigDir(), "voikko");
    private static final File DICTS_DIRECTORY = new File(VOIKKO_DIRECTORY, "dicts");

    static {
        if (System.getProperty("jna.library.path") == null) {
            System.setProperty("jna.library.path", VOIKKO_DIRECTORY.getAbsolutePath());
        }
    }

    public static Voikko getVoikko(String language) {
        if (voikko.containsKey(language)) {
            return voikko.get(language);
        }

        if (!isLanguageSupported(language)) {
            throw new VoikkoException("Voikko doesn't support " + language);
        }
        Voikko v = new Voikko(language, DICTS_DIRECTORY.getAbsolutePath());
        voikko.put(language, v);
        return v;
    }

    public static Stream<String> getSupportedLanguages() {
        return Voikko.listDicts(DICTS_DIRECTORY.getAbsolutePath()).stream().map(Dictionary::getLanguage);
    }

    public static boolean isLanguageSupported(String language) {
        return getSupportedLanguages().anyMatch(lang -> lang.equals(language));
    }
}
