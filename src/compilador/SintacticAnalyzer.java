/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 *
 * @author pietr
 */
public class SintacticAnalyzer {

    private LexicalAnalyzer lexical;
    private SemanticAnalyzer semantic;
    private ArrayList<String> errors = new ArrayList();
    private ArrayList<Token> tokens = new ArrayList();
    private ArrayList<String> sync = new ArrayList();
    private int current;
    private String escopoAtual = "global";

    public SintacticAnalyzer(LexicalAnalyzer lexical, SemanticAnalyzer semantic) {
        this.semantic = semantic;
        this.lexical = lexical;
        this.current = 0;
        errors.clear();
    }

    public void zzDiscardWhile(String... tokens) throws IOException {
        System.out.println("DESCARTANDO");

        Token t = this.getPreviousToken();

        while (!Arrays.asList(tokens).contains(t.getLexema())
                && !Arrays.asList(tokens).contains(t.getDescricao())
                && this.hasNextToken()) {
            t = this.getNextToken();
            if(this.current > this.tokens.size()){
                break;
            }
        }
        return;
    }

    public Token getPreviousToken() {
        return tokens.get(this.current - 1);
    }

    public Token getNextToken() {
        if (this.current < tokens.size()) {
            return tokens.get(this.current++);
        } else {
            System.out.println("CAIU NA EXCEPTION!!!");
            return tokens.get(0);
        }
    }

    public Token seeNextToken() {
        return tokens.get(this.current + 1);
    }

    public boolean hasNextToken() {
        return (this.current < this.tokens.size());
    }

    public boolean zzBegin() throws IOException {
        Token t = lexical.next();

        while (t != null) {
            tokens.add(t);
            t = lexical.next();
        }

        // bloco de declaração de variaveis
        t = programRule();

        blocoRule(t);

        return true;
    }

    private Token programRule() throws IOException {
        Token t = this.getNextToken();
        System.out.println("EXECUTANDO: REGRA DO PROGRAM com o token: " + t.getLexema());

        if (!t.is("program")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possível encontrar a palavra 'program', na linha " + t.getLine() + ".");
            zzDiscardWhile("IDENTIFICADOR");
        } else {
            t = this.getNextToken();
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou program!");
        }

        if (!t.is("IDENTIFICADOR")) {

            if (t.is("PALAVRA_RESERVADA")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Palavra reservada " + t.getLexema() + " não pode ser utilizada como identificador, na linha " + t.getLine() + ".");
            } else {
                errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possível encontrar o IDENTIFICADOR do programa, na linha " + t.getLine() + ".");
            }
            zzDiscardWhile(";", ",");

        } else {
            t = this.getNextToken();
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou identificador!");
        }

        if (!t.is(";")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Ponto e virgula esperado, mas foi encontrado um " + t.getLexema() + ", na linha " + t.getLine() + ".");
            zzDiscardWhile("int", "boolean");
        } else {
            t = this.getNextToken();
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou ponto-e-virgula!");
        }
        return t;
    }

    public Token blocoRule(Token t) throws IOException {

        System.out.println("Executando: REGRA DO BLOCO com o token: " + t.getLexema() + " e: " + t.getDescricao());
        t = declaracaoDeVariaveisRule(t);

        t = procedureRule(t);

        t = comandoRule(t);

        return t;
    }

    //<declaracaoDeVariaveis> ::= <tipo><lista de identificadores>
    private Token declaracaoDeVariaveisRule(Token t) throws IOException {

        while (isTipo(t)) {
            System.out.println("Executando: BLOCO DE DECLARACAO DE VARIAVEIS com o token: " + t.getLexema());

            errors.add("ESCOPO: " + this.escopoAtual + " - Casou tipo!");

            listaDeIdentificadoresRule(t.getLexema());

            t = getNextToken();
        }

        return t;
    }

    private boolean listaDeIdentificadoresRule(String tipo) throws IOException {
        while (true) {
            Token t = getNextToken();

            if (!t.is("IDENTIFICADOR")) {

                if (t.is("PALAVRA_RESERVADA")) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Palavra reservada " + t.getLexema() + " não pode ser um identificador, na linha " + t.getLine() + ".");
                    this.zzDiscardWhile(";", ",");
                } else {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Identificador não encontrado, na linha " + t.getLine() + ".");
                    this.zzDiscardWhile(";", ",");
                }
            } else {

                if (semantic.getEscopoWithName(this.escopoAtual).hasVarWithName(t.getLexema())) {
                    semantic.addError("ESCOPO: " + this.escopoAtual + " - Identificador já declarado, na linha " + t.getLine() + ".");
                } else {
                    semantic.addVariavel(this.escopoAtual, t, tipo);
                }

                errors.add("ESCOPO: " + this.escopoAtual + " - Casou IDENTIFICADOR:" + t.getLexema());
                t = getNextToken();
            }

            if (t.is(",")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Casou ,!");
                doNothing();
            } else if (t.is(";")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Casou ;!");
                return true;
            } else {
                errors.add("ESCOPO: " + this.escopoAtual + " - Virgula ou ponto e virgula não encontrado, na linha " + t.getLine() + ".");
            }

        }

    }

    private Token procedureRule(Token t) throws IOException {

        if (!t.is("procedure")) {
            return t;
        } else {
            System.out.println("Executando: REGRA DA PROCEDURE com o token: " + t.getLexema());
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou procedure");
            t = getNextToken();
        }

        if (!t.is("IDENTIFICADOR")) {
            if (t.is("PALAVRA_RESERVADA")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Palavra reservada " + t.getLexema() + " não pode ser utilizada como identificador, na linha " + t.getLine() + ".");
            } else {
                errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possível encontrar o IDENTIFICADOR, na linha " + t.getLine() + ".");
            }
            this.zzDiscardWhile("(");
        } else {
            this.escopoAtual = t.getLexema();
            this.semantic.addEscopo(this.escopoAtual);

            errors.add("ESCOPO: " + this.escopoAtual + " - Casou identificador!");
            t = getNextToken();
        }

        if (!t.is("AP")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o ABRE PARENTESES, na linha " + t.getLine() + ".");
            this.zzDiscardWhile("var", ")");
        } else {
            errors.add("Casou (!");
            t = getNextToken();
        }

        if (!t.is("var")) {
            if (!t.is(")")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o FECHA PARENTESES ou a PALAVRA RESERVADA var, na linha " + t.getLine() + ".");
                this.zzDiscardWhile(";");
            }
        } else {
            //Se Token for VAR, devemos ter um ou mais identificadores (<IDENTIFICADOR><virgula>)
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou palavra var!");

            t = getNextToken();

            do {
                if (!t.is("IDENTIFICADOR")) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o IDENTIFICADOR, na linha " + t.getLine() + ".");
                    this.zzDiscardWhile(":", ";", ",");
                } else {
                    if (semantic.getEscopoWithName(this.escopoAtual).hasVarWithName(t.getLexema())) {
                        semantic.addError("ESCOPO: " + this.escopoAtual + " - Identificador já declarado, na linha " + t.getLine() + ".");
                    } else {
                        semantic.addVariavel(this.escopoAtual, t, null);
                    }
                    errors.add("ESCOPO: " + this.escopoAtual + " - Casou IDENTIFICADOR: " + t.getLexema());
                    t = getNextToken();
                }

                if (!t.is(",")) {

                    if (t.is(":")) {
                        errors.add("ESCOPO: " + this.escopoAtual + " - Casou :");
                        t = getNextToken();
                    } else {
                        errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o ':' ou ','  na linha " + t.getLine() + ".");
                        this.zzDiscardWhile(";");
                    }

                } else {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Casou ,");
                    t = getNextToken();
                }

                if (!isTipo(t)) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o TIPO, na linha " + t.getLine() + ".");
                    this.zzDiscardWhile(")");
                } else {
                    //ENCONTROU O TIPO, TEMOS QUE SETAR TODOS OS TIPOS NULOS PARA O TIPO ENCONTRADO

                    semantic.getEscopoWithName(this.escopoAtual).changeTipoNullTo(t.getLexema());
                    semantic.addError("ESCOPO: " + this.escopoAtual + " - Tipo encontrado, todos os tipos nulos foram refatorados para: " + t.getLexema());
                    errors.add("ESCOPO: " + this.escopoAtual + " - Casou tipo");
                    t = getNextToken();
                    break;
                }

            } while (true);
        }

        if (!t.is(")")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o FECHA PARENTESES, na linha " + t.getLine() + ".");
            this.zzDiscardWhile(";");
        } else {
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou )");
            t = getNextToken();
        }

        if (!t.is(";")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o ';' , na linha " + t.getLine() + ".");
            this.zzDiscardWhile("int", "boolean");
        } else {
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou ;");
            t = getNextToken();
        }

        t = this.blocoRule(t);

        return t;
    }

    private Token comandoRule(Token t) throws IOException {
        System.out.println("Executando: REGRA DO COMANDO com o token: " + t.getLexema());
        if (t.is("begin")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Casou BEGIN");

            while (true) {
                if(this.current > this.tokens.size()){
                    break;
                }
                
                t = getNextToken();
                
                

                if (t.is("end")) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Casou END");
                    this.escopoAtual = "global";
                    semantic.addError("ESCOPO: " + escopoAtual + " - O escopo foi alterado.");
                    t = getNextToken();

                    if (!t.is(";")) {
                        errors.add("ESCOPO: " + this.escopoAtual + " - PONTO E VIRGULA não encontrado");
                    } else {
                        t = getNextToken();
                    }
                    break;
                }

                if (t.is("IDENTIFICADOR")) {
                    t = getNextToken();

                    if (t.is(":=")) {
                        errors.add("ESCOPO: " + this.escopoAtual + " - É atribuição.");
//                        atribuicao(t);
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
                    errors.add("ESCOPO: " + this.escopoAtual + " - Token inesperado");
                }
            }

        } else {
            errors.add("ESCOPO: " + this.escopoAtual + " - BEGIN não encontrado, foi encontrado: " + t.getLexema());
        }

        return t;
    }

    private void atribuicao(Token t) throws IOException {
        Token aux = t;

        t = lexical.next();

        if (seeNextToken().is(";")) {
//            É atribuição simples
            if (t.is("INTEIRO")) {
                if (aux.getTipo().equals("INTEIRO")) {
                    semantic.addError("ESCOPO: " + this.escopoAtual + " - OK Tipo: INTEIRO esperado, encontrado: " + t.getLexema());
                } else {
                    semantic.addError("ESCOPO: " + this.escopoAtual + " - ERRO Tipo: INTEIRO esperado, encontrado: " + t.getLexema());
                }
            }
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

}
