package net.briac.omegat.plugin.voikko;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omegat.util.StaticUtils;
import org.puimula.libvoikko.Voikko;
import org.puimula.libvoikko.VoikkoException;

public class VoikkoInstance {

    private Voikko voikko = null;

    private static final Logger LOGGER = Logger.getLogger(FinnishVoikkoTokenizer.class.getName());
    private static final File VOIKKO_DIRECTORY = new File(StaticUtils.getConfigDir(), "voikko");
    private static final File DICTS_DIRECTORY = new File(VOIKKO_DIRECTORY, "dicts");

    protected static final String FINNISH_LANGUAGE_CODE = "fi";
    protected static final String[] SUPPORTED_LANGUAGES = new String[] { FINNISH_LANGUAGE_CODE };
    
    static {
        if (System.getProperty("jna.library.path") == null) {
            System.setProperty("jna.library.path", VOIKKO_DIRECTORY.getAbsolutePath());
        }
    }

    private VoikkoInstance() {
        try {
            voikko = new Voikko(SUPPORTED_LANGUAGES[0], DICTS_DIRECTORY.getAbsolutePath());
        } catch (VoikkoException e) {
            LOGGER.log(Level.WARNING, "Could not load Voikko instance", e);
        }
    }

    private static class VoikkoHolder {
        private final static VoikkoInstance instance = new VoikkoInstance();
    }

    public static VoikkoInstance getInstance() {
        return VoikkoHolder.instance;
    }

    public Voikko getVoikko() {
        return voikko;
    }
}
