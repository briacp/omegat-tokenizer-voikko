/**************************************************************************
Voikko Finnish Tokenizer for OmegaT
Copyright (C) 2020 Briac Pilpre

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
**************************************************************************/
package net.briac.omegat.plugin.tokenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.omegat.core.Core;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.LuceneFinnishTokenizer;
import org.omegat.tokenizer.Tokenizer;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.puimula.libvoikko.Analysis;
import org.puimula.libvoikko.TokenType;
import org.puimula.libvoikko.Voikko;

@Tokenizer(languages = { "fi" }, isDefault = true)
public class FinnishVoikkoTokenizer extends DefaultTokenizer {
    private static final File VOIKKO_DIRECTORY = new File(StaticUtils.getConfigDir(), "voikko");
    private static final File DICTS_DIRECTORY = new File(VOIKKO_DIRECTORY, "dicts");
    private static final Logger LOGGER = Logger.getLogger(FinnishVoikkoTokenizer.class.getName());
    private static final String PLUGIN_NAME = FinnishVoikkoTokenizer.class.getPackage().getImplementationTitle();
    private static final String PLUGIN_VERSION = FinnishVoikkoTokenizer.class.getPackage().getImplementationVersion();

    private static final String FINNISH_LANGUAGE_CODE = "fi";
    private static final String[] SUPPORTED_LANGUAGES = new String[] { FINNISH_LANGUAGE_CODE };

    private final Map<String, Token[]> tokenCacheNone = new ConcurrentHashMap<>(2500);
    private final Map<String, Token[]> tokenCacheMatching = new ConcurrentHashMap<>(2500);
    private final Map<String, Token[]> tokenCacheGlossary = new ConcurrentHashMap<>(2500);

    protected static final String[] EMPTY_STRING_LIST = new String[0];
    protected static final Token[] EMPTY_TOKENS_LIST = new Token[0];
    protected static final int DEFAULT_TOKENS_COUNT = 64;

    private final Voikko voikko;
    
    static {
        if (System.getProperty("jna.library.path") == null) {
            System.setProperty("jna.library.path", VOIKKO_DIRECTORY.getAbsolutePath());
        }
    }

    public static void loadPlugins() {
        Core.registerTokenizerClass(FinnishVoikkoTokenizer.class);

        // Remove LuceneFinnishTokenizer as the default tokenizer
        PluginUtils.getTokenizerClasses().remove(LuceneFinnishTokenizer.class);
    }

    public static void unloadPlugins() {
        /* empty */
    }

    public FinnishVoikkoTokenizer() {
        super();
        LOGGER.info("Loading " + PLUGIN_NAME + " v." + PLUGIN_VERSION + " (Voikko directory: "
                + VOIKKO_DIRECTORY.getAbsolutePath() + ")");
        voikko = new Voikko(SUPPORTED_LANGUAGES[0], DICTS_DIRECTORY.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token[] tokenizeWords(String strOrig, StemmingMode stemmingMode) {
        Map<String, Token[]> cache;
        switch (stemmingMode) {
        case NONE:
            cache = tokenCacheNone;
            break;
        case GLOSSARY:
            cache = tokenCacheGlossary;
            break;
        case MATCHING:
            cache = tokenCacheMatching;
            break;
        default:
            throw new RuntimeException("No cache for specified stemming mode");
        }
        Token[] result = cache.get(strOrig);
        if (result != null) {
            return result;
        }

        result = tokenize(strOrig, stemmingMode == StemmingMode.GLOSSARY || stemmingMode == StemmingMode.MATCHING,
                stemmingMode == StemmingMode.MATCHING, stemmingMode != StemmingMode.GLOSSARY, true);

        // put result in the cache
        cache.put(strOrig, result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] tokenizeWordsToStrings(String str, StemmingMode stemmingMode) {
        return tokenizeToStrings(str, stemmingMode == StemmingMode.GLOSSARY || stemmingMode == StemmingMode.MATCHING,
                stemmingMode == StemmingMode.MATCHING, stemmingMode != StemmingMode.GLOSSARY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token[] tokenizeVerbatim(final String str) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_TOKENS_LIST;
        }

        return tokenize(str, false, false, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] tokenizeVerbatimToStrings(String str) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_STRING_LIST;
        }

        return tokenizeToStrings(str, false, false, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    private Token[] tokenize(final String strOrig, final boolean stemsAllowed, final boolean stopWordsAllowed,
            final boolean filterDigits, final boolean filterWhitespace) {
        if (StringUtil.isEmpty(strOrig)) {
            return EMPTY_TOKENS_LIST;
        }

        List<Token> result = new ArrayList<>(64);

        for (org.puimula.libvoikko.Token token : voikko.tokens(strOrig)) {
            if (acceptToken(token, filterDigits, filterWhitespace)) {
                if (stemsAllowed) {
                    for (Analysis analysis : voikko.analyze(token.getText())) {
                        if (analysis.containsKey("BASEFORM")) {
                            result.add(new Token(analysis.get("BASEFORM"), token.getStartOffset(),
                                    token.getEndOffset() - token.getStartOffset()));
                        }
                    }
                } else {
                    result.add(new Token(token.getText(), token.getStartOffset(),
                            token.getEndOffset() - token.getStartOffset()));
                }

            }
        }

        return result.toArray(new Token[result.size()]);
    }

    private boolean acceptToken(org.puimula.libvoikko.Token token, boolean filterDigits, boolean filterWhitespace) {
        if (filterWhitespace && token.getType() == TokenType.WHITESPACE) {
            return false;
        }

        if (!filterDigits) {
            return true;
        }

        String tokenText = token.getText();

        for (int i = 0, cp; i < tokenText.length(); i += Character.charCount(cp)) {
            cp = tokenText.codePointAt(i);
            if (Character.isDigit(cp)) {
                return false;
            }
        }
        return true;
    }

    protected String[] tokenizeToStrings(final String str, final boolean stemsAllowed, final boolean stopWordsAllowed,
            final boolean filterDigits, final boolean filterWhitespace) {
        if (StringUtil.isEmpty(str)) {
            return EMPTY_STRING_LIST;
        }

        List<String> result = new ArrayList<>(64);

        for (org.puimula.libvoikko.Token token : voikko.tokens(str)) {
            if (acceptToken(token, filterDigits, filterWhitespace)) {
                if (stemsAllowed) {
                    for (Analysis analysis : voikko.analyze(token.getText())) {
                        if (analysis.containsKey("BASEFORM")) {
                            result.add(analysis.get("BASEFORM"));
                        }
                    }
                } else {
                    result.add(token.getText());
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

}
