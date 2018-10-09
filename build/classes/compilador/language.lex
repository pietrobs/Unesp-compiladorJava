package compilador;
import javax.swing.table.DefaultTableModel;
import java_cup.runtime.Symbol;
import jflex.sym;

%%

%{


private DefaultTableModel table;

private void imprimir(String descricao, String lexema) {
    return new Token(lexema, descricao,true, yyline, yycolumn, yycolumn + lexema.length() - 1, -1);
}

LexicalAnalyzer(java.io.Reader in, DefaultTableModel table) {
    this.zzReader = in;
    this.table = table;
}

%}

%cup
%public
%class LexicalAnalyzer
%column
%line



LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

BRANCO = [ |\t|\r]
NL = [\n]
OPSOMA = "+"
OPSUB = "-"
OPDIV = "/"
OPMUL = "*"
AP = "("
FP = ")"
PONTO = "."

DIGITO = [0-9]

REAL = {DIGITO}+{PONTO}{DIGITO}+
INTEIRO = 0|{DIGITO}+

PALAVRA_RESERVADA = "program"|"procedure"|"begin"|"end"|"int"|"boolean"|"read"|"write"|"true"|"false"
            |"if"|"then"|"else"|"while"|"do"


IDENTIFICADOR = [a-zA-Z]([a-zA-Z]|[0-9])*
SIMBOLO_ESPECIAL = (> | < | <= | >= | := | ; | , | :)



%%

{Comment}                      {Tokenizer("COMENTARIO", yytext()); }
{BRANCO}                       {}
{NL}                           {}
{OPSOMA}                       { return Tokenizer("OPSOMA", yytext()); }
{OPSUB}                       { return Tokenizer("OPSUB", yytext()); }
{OPDIV}                       { return Tokenizer("OPDIV", yytext()); }
{OPMUL}                       { return Tokenizer("OPMUL", yytext()); }
{AP}                       { return Tokenizer("AP", yytext()); }
{FP}                       { return Tokenizer("FP", yytext()); }
{INTEIRO}                    { return Tokenizer("INTEIRO", yytext()); }
{REAL}                      { return Tokenizer("REAL", yytext()); }
{PALAVRA_RESERVADA}              { return Tokenizer("PALAVRA_RESERVADA", yytext()); }
{IDENTIFICADOR}                   { return Tokenizer("IDENTIFICADOR", yytext()); }
{SIMBOLO_ESPECIAL}                 { return Tokenizer("SIMBOLO_ESPECIAL", yytext());}
    
. { return Tokenizer("Caractere inválido", yytext()); }