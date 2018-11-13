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
public class SemanticAnalyzer {

    private ArrayList<Tabela> tabelas = new ArrayList();
    private ArrayList<String> errors = new ArrayList();

    public SemanticAnalyzer() {
        this.addEscopo("global");
    }

    public void addEscopo(String nome) {
        Tabela t = new Tabela(nome);
        if (!hasEscopoWithName(nome)) {
            errors.add("Escopo '" + nome + "' foi adicionado a tabela");
            tabelas.add(t);
        } else {
            errors.add("Escopo " + nome + " já declarado!");
        }
    }

    public Tabela getEscopoWithName(String nome) {
        for (Tabela t : tabelas) {
            if (t.getNome().equals(nome)) {
                return t;
            }
        }
        return null;
    }

    public boolean hasEscopoWithName(String nome) {
        for (Tabela t : tabelas) {
            if (t.getNome().equals(nome)) {
                return true;
            }
        }
        return false;
    }
    
    public void addError(String error){
        this.errors.add(error);
    }

    public String getErrors() {
        String r = "";
        for (int i = 0; i < this.errors.size(); i++) {
            r += errors.get(i) + "\n";
        }
        return r;
    }

    public void addVariavel(String nome, Token t, String tipo) {
        for (int i = 0; i < tabelas.size(); i++) {
            if (tabelas.get(i).getNome().equals(nome)) {
                tabelas.get(i).addVar(t, tipo);
                errors.add("ESCOPO: " + nome + " teve uma variavel adicionada: " + t.getLexema() +" do tipo " + tipo);
            }
        }
    }
}
