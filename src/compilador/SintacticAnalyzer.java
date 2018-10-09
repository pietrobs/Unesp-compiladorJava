/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author pietr
 */
public class SintacticAnalyzer {

    private LexicalAnalyzer lexical;
    private ArrayList<String> errors = new ArrayList();

    public SintacticAnalyzer(LexicalAnalyzer lexical) {
        this.lexical = lexical;
        errors.clear();
    }

    public void zzDiscardBlock(String token) throws IOException {
        while (true) {
            if (lexical.next().is(token)) {
                return;
            }
        }
    }

    public void zzDiscardRow(int current_line) throws IOException {
        Token t;
        while (true) {

            t = lexical.next();
            if (current_line == t.getLine()) {
//                JOptionPane.showMessageDialog(null, "Current line: " + current_line + "|" + "Line:" + t.getLine() + " | " + t.getLexema());
            } else {
                return;
            }
        }
    }

    public boolean zzBegin() throws IOException {
        Token t = null;

        // bloco de declaração de variaveis
        t = programRule();
        blocoRule(t);

        return true;
    }

    private Token programRule() throws IOException {
        Token t = lexical.next();

        if (!t.is("program")) {
            errors.add("Não foi possível encontrar a palavra 'program', na linha " + t.getLine() + ".");
        } else {
            t = lexical.next();
            errors.add("Casou program!");
        }

        if (!t.is("IDENTIFICADOR")) {
            if (t.is("PALAVRA_RESERVADA")) {
                errors.add("Palavra reservada " + t.getLexema() + " não pode ser utilizada como identificador, na linha " + t.getLine() + ".");
            } else {
                errors.add("Não foi possível encontrar o IDENTIFICADOR do programa, na linha " + t.getLine() + ".");
            }
        } else {
            t = lexical.next();
            errors.add("Casou identificador!");
        }

        if (!t.is(";")) {
            errors.add("Ponto e virgula esperado, mas foi encontrado um " + t.getLexema() + ", na linha " + t.getLine() + ".");
            boolean continuar = true;

            do {
                if (isTipo(t)) {
                    errors.add("Casou tipo!");
                    continuar = false;
                    declaracaoDeVariaveisRule(t);
                } else if (t.is("procedure")) {
                    continuar = false;
                    //declaracaoDeSubRotinas();
                    errors.add("Bloco de subrotinas não implementado ainda, na linha " + t.getLine() + ".");
                } else if (t.is("begin")) {
                    continuar = false;
                    //declaracaoDeProgramas();
                    errors.add("Bloco do programa não implementado ainda, na linha " + t.getLine() + ".");
                }
                t = lexical.next();
            } while (!t.is(";") && continuar);

        } else {
            errors.add("Casou ponto-e-virgula!");
        }
        return t;
    }

    public void blocoRule(Token t) throws IOException {
        t = lexical.next();

        t = declaracaoDeVariaveisRule(t);

        t = procedureRule(t);

        t = comandoRule(t);
    }

    //<declaracaoDeVariaveis> ::= <tipo><lista de identificadores>
    private Token declaracaoDeVariaveisRule(Token t) throws IOException {

        boolean continuar;
        while (isTipo(t)) {
            errors.add("Casou tipo!");
            listaDeIdentificadoresRule();
            t = this.lexical.next();
        }

        return t;
    }

    private boolean listaDeIdentificadoresRule() throws IOException {
        while (true) {
            Token t = this.lexical.next();
            if (t.is("IDENTIFICADOR")) {
                errors.add("Casou identificador!");
                t = this.lexical.next();

                if (t.is(",")) {
                    errors.add("Casou ,!");
                    doNothing();
                } else if (t.is(";")) {
                    errors.add("Casou ;!");
                    return true;
                } else {
                    errors.add("Virgula ou ponto e virgula não encontrado, na linha " + t.getLine() + ".");
                }

            } else if (t.is("PALAVRA_RESERVADA")) {
                errors.add("Palavra reservada " + t.getLexema() + " não pode ser um identificador, na linha " + t.getLine() + ".");
                this.zzDiscardBlock(";");
                return false;
            } else {
                errors.add("Identificador não encontrado, na linha " + t.getLine() + ".");
                this.zzDiscardBlock(";");
                return false;
            }
        }
    }

    private Token procedureRule(Token t) throws IOException {

        if (!t.is("procedure")) {
            return t;
        } else {
            errors.add("Casou procedure");
            t = lexical.next();
        }

        if (!t.is("IDENTIFICADOR")) {
            if (t.is("PALAVRA_RESERVADA")) {
                errors.add("Palavra reservada " + t.getLexema() + " não pode ser utilizada como identificador, na linha " + t.getLine() + ".");
            } else {
                errors.add("Não foi possível encontrar o IDENTIFICADOR do programa, na linha " + t.getLine() + ".");
            }
        } else {
            t = lexical.next();
            errors.add("Casou identificador!");
        }

        if (!t.is("AP")) {

        } else {
            errors.add("Casou (!");
            t = lexical.next();
        }

        boolean encontrouFP = false;

        do {
            if (!t.is("var")) {
                break;
            } else {
                //Se Token for VAR, devemos ter um ou mais identificadores (<IDENTIFICADOR><virgula>)
                errors.add("Casou palavra var!");
                t = lexical.next();

                do {
                    if (t.is("IDENTIFICADOR")) {
                        errors.add("Casou identificador!");
                        t = lexical.next();

                        if (t.is(",")) {
                            errors.add("Casou virgula!");
                            t = lexical.next();
                        } else if (t.is(":")) {
                            t = lexical.next();
                            errors.add("Casou :!");

                            if (isTipo(t)) {
                                t = lexical.next();
                                errors.add("Casou tipo!");
                            }
                        } else {
//                            errors.add("");
                        }

                    }

                    if (t.is(";")) {
                        errors.add("Casou ;!");
                        t = lexical.next();
                        break;
                    } else if (t.is(")")) {
                        encontrouFP = true;
                        errors.add("Casou )!");
                        t = lexical.next();
                        break;
                    }

                } while (true);
            }

            if (encontrouFP) {
                break;
            }

        } while (true);

        if (t.is(";")) {
            errors.add("Casou ;!");
            t = lexical.next();
        } else {
//                t = lexical.next();
        }

        this.blocoRule(t);

        return t;
    }

    private Token comandoRule(Token t) throws IOException {
        if (t.is("begin")) {

            while (true) {

                t = lexical.next();

                if (t.is("end")) {
                    break;
                }

                if (t.is("IDENTIFICADOR")) {
                    t = lexical.next();

                    if (t.is(":=")) {
                        atribuicao(t);
                    } else if (t.is("(")) {
//                      chamada de procedimento
                    }
//                    
                } else if (t.is("begin")) {
                    comandoRule(t);
                } else if (t.is("if")) {
//                    comando condicional
                } else if (t.is("while")) {
//                    comando repetitivo
                } else {
                    errors.add("Token inesperado");
                }
            }

        } else {
            errors.add("BEGIN não encontrado");
        }
        return t;
    }

    private void atribuicao(Token t) throws IOException {
        t = lexical.next();

//        expressaoSimples(t);
        if (isRelacao(t)) {

//            expressaoSimples(t);
        }

    }

    private void expressaoSimples(Token t) {
        
    }

//    FUNÇÕES AUXILIARES
    private boolean isTipo(Token t) {
        if (t.is("boolean") || t.is("int")) {
            return true;
        }
        return false;
    }

    private boolean isTermo(Token t) {
        if (t.is("IDENTIFICADOR") || t.is("REAL") || t.is("INTEIRO")) {
            return true;
        }
        return false;
    }

    private boolean isRelacao(Token t) {
        if (t.is("=") || t.is("<>") || t.is("<=") || t.is(">=") || t.is(">")) {
            return true;
        }
        return false;
    }

    private void doNothing() {

    }

    public String getErrors() {
        String r = "";
        for (int i = 0; i < this.errors.size(); i++) {
            r += errors.get(i) + "\n";
        }
        return r;
    }

    public void discardWhile(ArrayList<String> seguintes, ArrayList<String> esperado) {

    }

}
