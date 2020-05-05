/**************************************************************************
Voikko Tokenizer for OmegaT
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

public class VoikkoGrammarCheck implements IIssueProvider, IMarker {

    private static final String ERROR_MESSAGE_LOCALE = "en";
    static final HighlightPainter PAINTER = new UnderlineFactory.WaveUnderline(
            Styles.EditorColor.COLOR_LANGUAGE_TOOLS.getColor().darker());

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getName() {
        return "Voikko Grammar";
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
            m.painter = PAINTER;
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

        if (targetLang == null || !VoikkoInstance.isLanguageSupported(targetLang)) {
            return Collections.emptyList();
        }

        return VoikkoInstance.getVoikko(targetLang).grammarErrors(translationText, ERROR_MESSAGE_LOCALE);
    }

}
