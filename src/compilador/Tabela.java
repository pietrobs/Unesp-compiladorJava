/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.util.ArrayList;

/**
 *
 * @author pietr
 */
public class Tabela {

    private String nome;
    private ArrayList<Token> variaveis = new ArrayList();

    public Tabela(String nome) {
        this.nome = nome;
    }

    public void addVar(Token t, String tipo) {
        t.setTipo(tipo);
        variaveis.add(t);
    }
    
    public boolean hasVarWithName(String varName){
        for(int i = 0; i < variaveis.size(); i++){
            if(variaveis.get(i).getLexema().equals(varName)){
                return true;
            }
        }
        return false;
    }
    
    public void changeTipoNullTo(String tipo){
        for(int i = 0; i < variaveis.size(); i++){
            if(variaveis.get(i).getTipo() == null){
                variaveis.get(i).setTipo(tipo);
            }
        }
    }
    
    public boolean isValid(Token t){
        return t.getDescricao().equals("IDENTIFICADOR");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public ArrayList<Token> getVariaveis() {
        return variaveis;
    }

    public void setVariaveis(ArrayList<Token> variaveis) {
        this.variaveis = variaveis;
    }
    
    
}
