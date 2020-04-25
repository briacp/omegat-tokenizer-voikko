/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Briac Pilpre
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package net.briac.omegat.plugin.voikko;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.issues.IIssue;
import org.omegat.gui.issues.IssueDetailSplitPanel;
import org.omegat.gui.issues.SimpleColorIcon;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles.EditorColor;
import org.puimula.libvoikko.GrammarError;

class VoikkoIssue implements IIssue {

    static final Icon ICON = new SimpleColorIcon(EditorColor.COLOR_LANGUAGE_TOOLS.getColor().brighter());
    static final AttributeSet ERROR_STYLE;
    static {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, EditorColor.COLOR_LANGUAGE_TOOLS.getColor().brighter());
        StyleConstants.setBold(attr, true);
        ERROR_STYLE = attr;
    }

    private final SourceTextEntry ste;
    private final String targetText;
    private final GrammarError result;

    VoikkoIssue(SourceTextEntry ste, String targetText, GrammarError match) {
        this.ste = ste;
        this.targetText = targetText;
        this.result = match;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getTypeName() {
        return "Voikko";
    }

    @Override
    public int getSegmentNumber() {
        return ste.entryNum();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(result.getShortDescription());
        if (!result.getSuggestions().isEmpty()) {
            sb.append(" - <i>");
            result.getSuggestions().forEach(s -> {
                sb.append(s);
                sb.append(" ");
            });
            sb.append("</i>");
        }

        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public Component getDetailComponent() {
        IssueDetailSplitPanel panel = new IssueDetailSplitPanel();
        panel.firstTextPane.setText(ste.getSrcText());
        panel.lastTextPane.setText(targetText);
        StyledDocument doc = panel.lastTextPane.getStyledDocument();
        doc.setCharacterAttributes(result.getStartPos(), result.getErrorLen(), ERROR_STYLE, false);
        panel.setMinimumSize(new Dimension(0, panel.firstTextPane.getFont().getSize() * 6));
        return panel;
        
    }

    @Override
    public boolean hasMenuComponents() {
        return false;
    }

    @Override
    public List<? extends JMenuItem> getMenuComponents() {
        return Collections.emptyList();
    }
}