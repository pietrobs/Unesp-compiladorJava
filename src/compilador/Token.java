/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

/**
 *
 * @author pietr
 */
public class Token {
    private String lexema;
    private String descricao;
    private boolean isTerminal;
    private int line;
    private int columnInit;
    private int columnEnd;
    private int valor;
    private String tipo;
    private boolean foiUtilizado;
    
    public Token(String lexema, String descricao, boolean isTerminal, int line, int columnInit, int columnEnd, int valor) {
        this.lexema = lexema;
        this.descricao = descricao;
        this.isTerminal = isTerminal;
        this.line = line;
        this.columnInit = columnInit;
        this.columnEnd = columnEnd;
        this.valor = valor;
        this.tipo = null;
        this.foiUtilizado = false;
    }
    
    public Object row(){
        Object[] line = new Object[6];
        
        line[0] = this.lexema;
        line[1] = this.descricao;
        line[2] = "-";
        line[3] = this.line;
        line[4] = this.columnInit;
        line[5] = this.columnEnd;
        
        return line;
    }
    
    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isIsTerminal() {
        return isTerminal;
    }

    public void setIsTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumnInit() {
        return columnInit;
    }

    public void setColumnInit(int columnInit) {
        this.columnInit = columnInit;
    }

    public int getColumnEnd() {
        return columnEnd;
    }

    public void setColumnEnd(int columnEnd) {
        this.columnEnd = columnEnd;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
    
    public boolean is(String s){
        return(this.descricao.equals(s) || this.lexema.equals(s)); 
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    
    
    
    
    
    
}
