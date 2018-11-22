/*
 * To change this license header, choose License Headers in Project_global Properties.
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
    private int count_token;
    private Token t_global = null;
    private String escopoAtual = "global";

    public SintacticAnalyzer(LexicalAnalyzer lexical, SemanticAnalyzer semantic) {
        this.semantic = semantic;
        this.lexical = lexical;
        this.count_token = 0;
        errors.clear();
    }

    public void zzDiscardWhile(String... tokens) throws IOException {
        t_global = this.getPreviousToken();

        while (!Arrays.asList(tokens).contains(t_global.getLexema())
                && !Arrays.asList(tokens).contains(t_global.getDescricao())
                && this.hasNextToken()) {
            t_global = this.getNextToken();
            if (this.count_token > this.tokens.size()) {
                break;
            }
        }
        t_global = this.getPreviousToken();
        return;
    }

    public Token getPreviousToken() {
        return tokens.get(this.count_token - 1);
    }

    public Token getNextToken() {
        if (this.count_token < tokens.size()) {
            return tokens.get(this.count_token++);
        } else {
            System.out.println("CAIU NA EXCEPTION!!!");
            return tokens.get(count_token - 1);
        }
    }

    public Token seeNextToken() {
        return tokens.get(this.count_token + 1);
    }

    public boolean hasNextToken() {
        return (this.count_token < this.tokens.size());
    }

    public boolean zzBegin() throws IOException {
        this.count_token = 0;
        Token t = lexical.next();
        while (t != null) {
            tokens.add(t);
            t = lexical.next();
        }

        // bloco de declaração de variaveis
        programRule();

        blocoRule();

        return true;
    }

    private void programRule() throws IOException {
        t_global = this.getNextToken();

        System.out.println("EXECUTANDO: REGRA DO PROGRAM com o token: " + t_global.getLexema());

        if (!t_global.is("program")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possível encontrar a palavra 'program', na linha " + t_global.getLine() + ".");
//            pintaLinha(t_global.getLine());
            zzDiscardWhile("IDENTIFICADOR");
        } else {
            t_global = this.getNextToken();
        }

        if (!t_global.is("IDENTIFICADOR")) {

            if (t_global.is("PALAVRA_RESERVADA")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Palavra reservada " + t_global.getLexema() + " não pode ser utilizada como identificador, na linha " + t_global.getLine() + ".");
            } else {
                errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possível encontrar o IDENTIFICADOR do programa, na linha " + t_global.getLine() + ".");
            }
            zzDiscardWhile(";", ",");

        } else {
            t_global = this.getNextToken();
        }

        if (!t_global.is(";")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Ponto e virgula esperado, mas foi encontrado um " + t_global.getLexema() + ", na linha " + t_global.getLine() + ".");
            zzDiscardWhile("int", "boolean");
        } else {
            t_global = this.getNextToken();
        }

    }

    public void blocoRule() throws IOException {

        declaracaoDeVariaveisRule();

        procedureRule();

        comandoRule();

    }

    //<declaracaoDeVariaveis> ::= <tipo><lista de identificadores>
    private void declaracaoDeVariaveisRule() throws IOException {

        while (isTipo(t_global)) {
            System.out.println("Executando: BLOCO DE DECLARACAO DE VARIAVEIS com o token: " + t_global.getLexema());

            listaDeIdentificadoresRule(t_global.getLexema());

            t_global = getNextToken();
        }

    }

    private boolean listaDeIdentificadoresRule(String tipo) throws IOException {
        while (true) {
            Token t_global = getNextToken();

            if (!t_global.is("IDENTIFICADOR")) {

                if (t_global.is("PALAVRA_RESERVADA")) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Palavra reservada " + t_global.getLexema() + " não pode ser um identificador, na linha " + t_global.getLine() + ".");
                    this.zzDiscardWhile(";", ",");
                } else {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Identificador não encontrado, na linha " + t_global.getLine() + ".");
                    this.zzDiscardWhile(";", ",");
                }
            } else {

                if (semantic.getEscopoWithName(this.escopoAtual).hasVarWithName(t_global.getLexema())) {
                    semantic.addError("ESCOPO: " + this.escopoAtual + " - Identificador já declarado, na linha " + t_global.getLine() + ".");
                } else {
                    semantic.addVariavel(this.escopoAtual, t_global, tipo);
                }
                t_global = getNextToken();
            }

            if (t_global.is(",")) {
                doNothing();
            } else if (t_global.is(";")) {
                return true;
            } else {
                errors.add("ESCOPO: " + this.escopoAtual + " - Virgula ou ponto e virgula não encontrado, na linha " + t_global.getLine() + ".");
            }

        }

    }

    private void procedureRule() throws IOException {

        if (!t_global.is("procedure")) {
            return;
        } else {
            System.out.println("Executando: REGRA DA PROCEDURE com o token: " + t_global.getLexema());
            t_global = getNextToken();
        }

        if (!t_global.is("IDENTIFICADOR")) {
            if (t_global.is("PALAVRA_RESERVADA")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Palavra reservada " + t_global.getLexema() + " não pode ser utilizada como identificador, na linha " + t_global.getLine() + ".");
            } else {
                errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possível encontrar o IDENTIFICADOR, na linha " + t_global.getLine() + ".");
            }
            this.zzDiscardWhile("(");
        } else {
            this.escopoAtual = t_global.getLexema();
            this.semantic.addEscopo(this.escopoAtual);

            t_global = getNextToken();
        }

        if (!t_global.is("AP")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o ABRE PARENTESES, na linha " + t_global.getLine() + ".");
            this.zzDiscardWhile("var", ")");
        } else {
            t_global = getNextToken();
        }

        if (!t_global.is("var")) {
            if (!t_global.is(")")) {
                errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar a PALAVRA RESERVADA var, na linha " + t_global.getLine() + ".");
                this.zzDiscardWhile("IDENTIFICADOR", ")");
            }
        } else {
            //Se Token for VAR, devemos ter um ou mais identificadores (<IDENTIFICADOR><virgula>)

            t_global = getNextToken();

            do {
                if (!t_global.is("IDENTIFICADOR")) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o IDENTIFICADOR, na linha " + t_global.getLine() + ".");
                    this.zzDiscardWhile(":", ";", ",");
                } else {
                    if (semantic.getEscopoWithName(this.escopoAtual).hasVarWithName(t_global.getLexema())) {
                        semantic.addError("ESCOPO: " + this.escopoAtual + " - Identificador já declarado, na linha " + t_global.getLine() + ".");
                    } else {
                        semantic.addVariavel(this.escopoAtual, t_global, null);
                    }
                    t_global = getNextToken();
                }

                if (!t_global.is(",")) {

                    if (t_global.is(":")) {
                        t_global = getNextToken();
                    } else {
                        errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o ':' ou ','  na linha " + t_global.getLine() + ".");
                        this.zzDiscardWhile(";", "int");
                    }

                } else {
                    t_global = getNextToken();
                }

                if (!isTipo(t_global)) {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o TIPO, na linha " + t_global.getLine() + ".");
                    this.zzDiscardWhile(")");
                } else {
                    //ENCONTROU O TIPO, TEMOS QUE SETAR TODOS OS TIPOS NULOS PARA O TIPO ENCONTRADO

                    semantic.getEscopoWithName(this.escopoAtual).changeTipoNullTo(t_global.getLexema());
                    semantic.addError("ESCOPO: " + this.escopoAtual + " - Tipo encontrado, todos os tipos nulos foram refatorados para: " + t_global.getLexema());
                    t_global = getNextToken();
                    break;
                }

            } while (true);
        }

        if (!t_global.is(")")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o FECHA PARENTESES, na linha " + t_global.getLine() + ".");
            this.zzDiscardWhile(";");
        } else {
            t_global = getNextToken();
        }

        if (!t_global.is(";")) {
            errors.add("ESCOPO: " + this.escopoAtual + " - Não foi possivel encontrar o ';' , na linha " + t_global.getLine() + ".");
            this.zzDiscardWhile("int", "boolean");
        } else {
            t_global = getNextToken();
        }

        this.blocoRule();
    }

    private void comandoRule() throws IOException {
        System.out.println("Executando: REGRA DO COMANDO com o token: " + t_global.getLexema());

        if (t_global.is("begin")) {

            while (true) {

                if (this.count_token >= this.tokens.size()) {
                    break;
                }

                t_global = getNextToken();

                if (t_global.is("end")) {
                    this.escopoAtual = "global";
                    semantic.addError("ESCOPO: " + escopoAtual + " - O escopo foi alterado.");
                    t_global = getNextToken();

                    if (!t_global.is(";")) {
                        errors.add("ESCOPO: " + this.escopoAtual + " - PONTO E VIRGULA não encontrado");
                    } else {
                        t_global = getNextToken();
                    }
                    break;
                }

                if (t_global.is("IDENTIFICADOR")) {
                    t_global = getNextToken();

                    if (t_global.is(":=")) {
                        System.out.println(t_global.getLexema());
                        expressao();
                    } else if (t_global.is("(")) {
//                      chamada de procedimento
                    }
//                    
                } else if (t_global.is("begin")) {
                    comandoRule();
                } else if (t_global.is("if")) {
//                    comando condicional
                } else if (t_global.is("while")) {
//                    comando repetitivo
                } else {
                    errors.add("ESCOPO: " + this.escopoAtual + " - Token inesperado : " + t_global.getLexema());
                }
            }

        } else {
            errors.add("ESCOPO: " + this.escopoAtual + " - BEGIN não encontrado, foi encontrado: " + t_global.getLexema());
        }

    }

//    private void atribuicao(Token t) throws IOException {
//        Token aux = t;
//
//        t_global = lexical.next();
//
//        if (seeNextToken().is(";")) {
////            É atribuição simples
//            if (t_global.is("INTEIRO")) {
//                if (aux.getTipo().equals("INTEIRO")) {
//                    semantic.addError("ESCOPO: " + this.escopoAtual + " - OK Tipo: INTEIRO esperado, encontrado: " + t_global.getLexema());
//                } else {
//                    semantic.addError("ESCOPO: " + this.escopoAtual + " - ERRO Tipo: INTEIRO esperado, encontrado: " + t_global.getLexema());
//                }
//            }
//        }
//
//    }
    private void isWhile(Token t) {

    }

    private void expressao() throws IOException {
        expressaoSimples();

        if (isRelacao()) {
            t_global = getNextToken();
            expressaoSimples();
        }
    }

    private void expressaoSimples() throws IOException {
        t_global = getNextToken();


        if (isSinal()) {
            t_global = getNextToken();
        }

        if (!isTermo()) {
            errors.add("ESCOPO: " + this.escopoAtual + " - ERRO Tipo: Atribuição inválida, TERMO esperado, encontrado:" + t_global.getLexema());
        } else {
            while (true) {
                t_global = getNextToken();
                if (t_global.is("+") || t_global.is("-") || t_global.is("or")) {
                    t_global = getNextToken();
                    if (!isTermo()) {
                        errors.add("ESCOPO: " + this.escopoAtual + " - ERRO Tipo: Atribuição inválida, TERMO esperado, encontrado:" + t_global.getLexema());
                    }
                } else {
                    break;
                }
            }
        }

    }

//    FUNÇÕES AUXILIARES
    private boolean isTipo(Token t) {
        if (t_global.is("boolean") || t_global.is("int")) {
            return true;
        }
        return false;
    }

    private boolean isTermo() {

        if (isFator()) {

            while (true) {
                t_global = getNextToken();
                if (t_global.is("*") || t_global.is("div") || t_global.is("and")) {
                    t_global = getNextToken();
                    if (!isFator()) {
                        return false;
                    }
                } else {
                    break;
                }
            }

        } else {
            errors.add("ESCOPO: " + this.escopoAtual + " - ERRO Tipo: Atribuição inválida, FATOR esperado, encontrado:" + t_global.getLexema());
        }
        return true;
    }

    private boolean isRelacao() {
        if (t_global.is("=") || t_global.is("<>") || t_global.is("<=") || t_global.is(">=") || t_global.is(">")) {
            return true;
        }
        return false;
    }

    private boolean isSinal() {
        if (t_global.is("+") || t_global.is("-")) {
            return true;
        }
        return false;
    }

    private boolean isFator() {
        if (t_global.is("IDENTIFICADOR") || t_global.is("REAL") || t_global.is("INTEIRO") || t_global.is("true") || t_global.is("false")) {
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
