/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author pietr
 */
public class PintadorDePalavras extends JTextPane {

    public void appendNaive(Color c, String s) {
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, c);

        int len = getText().length();
        setCaretPosition(len);
        setCharacterAttributes(aset, false);
        replaceSelection(s);
    }

    public void append(Color c, String s) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        int len = getDocument().getLength();
        setCaretPosition(len);
        setCharacterAttributes(aset, false);
        replaceSelection(s);
        
        StyleContext sc2 = StyleContext.getDefaultStyleContext();
        AttributeSet aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.black);
    }

    
}
