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
package net.briac.omegat.plugin.voikko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.issues.IIssue;
import org.omegat.gui.issues.IIssueProvider;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.Styles;
import org.puimula.libvoikko.GrammarError;
import org.puimula.libvoikko.Token;
import org.puimula.libvoikko.TokenType;
import org.puimula.libvoikko.Voikko;

// The ISpellChecker interface can't be used as a plugin...
public class VoikkoSpellCheck implements IIssueProvider, IMarker {

    private static final String VOIKKO_SPELLCHECK = "Voikko SpellCheck";

    static final HighlightPainter SPELLCHECK = new UnderlineFactory.WaveUnderline(
            Styles.EditorColor.COLOR_SPELLCHECK.getColor().darker());

    public VoikkoSpellCheck() {
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getName() {
        return VOIKKO_SPELLCHECK;
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive)
            throws Exception {
        if (translationText == null) {
            // Return when disabled or translation text is empty
            return null;
        }

        translationText = StringUtil.normalizeUnicode(translationText);
        sourceText = ste.getSrcText();

        return getCheckResults(sourceText, translationText).stream().map(match -> {
            Mark m = new Mark(Mark.ENTRY_PART.TRANSLATION, match.getStartPos(),
                    match.getStartPos() + match.getErrorLen());
            m.toolTipText = match.getShortDescription();
            m.painter = SPELLCHECK;
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
        try {
            return getCheckResults(sourceEntry.getSrcText(), tmxEntry.translation).stream().map(match -> {
                return new VoikkoIssue(sourceEntry, tmxEntry.translation, match);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    protected List<GrammarError> getCheckResults(String sourceText, String translationText) throws Exception {
        String targetLang = Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();

        if (targetLang == null || !targetLang.equalsIgnoreCase(VoikkoInstance.FINNISH_LANGUAGE_CODE)) {
            return Collections.emptyList();
        }

        if (translationText == null || translationText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Voikko voikko = VoikkoInstance.getInstance().getVoikko();
        List<GrammarError> errors = new ArrayList<>();

        for (Token token : voikko.tokens(translationText)) {
            if (token.getType() == TokenType.WORD) {
                if (!voikko.spell(token.getText())) {
                    errors.add(new GrammarError(1, token.getStartOffset(),
                            token.getEndOffset() - token.getStartOffset(), Collections.emptyList(),
                            voikko.suggest(token.getText()).stream().collect(Collectors.joining(", "))));
                }
            }
        }
        return errors;
    }

}
