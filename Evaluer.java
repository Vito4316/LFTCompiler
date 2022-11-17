import java.io.*; 

public class Evaluer {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    static final int fact = 0, term = 1, expr = 2, start = 3, exprp = 4, termp = 5;

    private void checkGuide(int func) {
        switch (func) {
            case fact: case term: case expr: case start:
                switch (look.tag) {
                    case '(', Tag.NUM -> {}
                    default->error("Error, expected '(' or number instead of " +  look);
                }
                break;

            case exprp:
                switch (look.tag) {
                    case '+', '-', ')', Tag.EOF -> {}
                    default -> error("Error, expected '+', '-' or ')' instead of " +  look);
                }
                break;

            case termp:
                switch (look.tag) {
                    case '+', '-', ')', '*', '/', Tag.EOF -> {}
                    default -> error("Error, expected operator or ')' instead of " +  look);
                }
                break;
                
            default:
                error("Bad defined guide switch");
        }
    }
    

    public Evaluer(Lexer l, BufferedReader br) { 
        lex = l; 
        pbr = br;
        move(); 
    }
   
    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF) move();
        } else error("syntax error" + look);
    }

    public void start() { 
        checkGuide(start);
	    int expr_val;
    	expr_val = expr();
	    
        match(Tag.EOF);

        System.out.println(expr_val);
    }

    private int expr() { 
        checkGuide(expr);
	    int term_val, exprp_val;

    	term_val = term();
	    exprp_val = exprp(term_val);

	    return exprp_val;
    }

    private int exprp(int exprp_i) {
        checkGuide(exprp);
        int term_val, exprp_val;
        switch (look.tag) {
            case '+':
                match('+');
                term_val = term();
                exprp_val = exprp(exprp_i + term_val);
                break;
            case '-':
                match('+');
                term_val = term();
                exprp_val = exprp(exprp_i + term_val);
                break;
            default:
                exprp_val = exprp_i;
                break;
        }
        return exprp_val;
    }

    private int term() { 
        checkGuide(term);
        int termp_i, term_val;
        termp_i = fact();
        term_val = termp(termp_i);
        return term_val;
    }
    
    private int termp(int termp_i) { 
        checkGuide(termp);
        int termp_val, fact_val;
        switch (look.tag) {
            case '*':
                match('*');
                fact_val = fact();
                termp_val = termp(termp_i * fact_val);
                break;
            case '/':
                match('/');
                fact_val = fact();
                termp_val = termp(termp_i / fact_val);
                break;
            default:
                termp_val = termp_i;
                break;
        }
        return termp_val;
    }
    
    private int fact() { 
        checkGuide(fact);
        int fact_val, expr_val;
        switch(look.tag) {
            case '(':
                match('(');
                expr_val = expr();
                match(')');
                fact_val = expr_val;
                break;
            case Tag.NUM:
                fact_val = ((NumberTok) look).getVal();
                match(Tag.NUM);
                break;
            default:
                error("Fact is not nullable");
                fact_val = -1;
        }
        return fact_val;
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "/home/vito/Projects/LFTCompiler/file"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Evaluer valutatore = new Evaluer(lex, br);
            valutatore.start();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}