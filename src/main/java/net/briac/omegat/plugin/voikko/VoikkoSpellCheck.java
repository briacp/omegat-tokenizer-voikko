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

import java.util.List;

import javax.swing.text.Highlighter.HighlightPainter;

import org.omegat.core.spellchecker.ISpellCheckerProvider;
import org.omegat.core.spellchecker.SpellCheckerException;
import org.omegat.gui.editor.UnderlineFactory;
import org.omegat.util.gui.Styles;
import org.puimula.libvoikko.Voikko;

public class VoikkoSpellCheck implements ISpellCheckerProvider {

    static final HighlightPainter SPELLCHECK = new UnderlineFactory.WaveUnderline(
            Styles.EditorColor.COLOR_SPELLCHECK.getColor().darker());
    private Voikko voikko;

    @Override
    public boolean isCorrect(String word) {
        return voikko.spell(word);
    }

    @Override
    public List<String> suggest(String word) {
        return voikko.suggest(word);
    }

    @Override
    public void learnWord(String word) {
        /* empty */
    }

    @Override
    public void init(String language) throws SpellCheckerException {
        if (VoikkoInstance.isLanguageSupported(language)) {
            voikko = VoikkoInstance.getVoikko(language);
        } else {
            throw new SpellCheckerException("Installed voikko dictionaries don't support " + language);
        }
    }

    @Override
    public void destroy() {
        /* empty */
    }

}
