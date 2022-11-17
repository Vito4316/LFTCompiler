import java.io.*;

public class EvaluerOld {
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
    
    public EvaluerOld(Lexer l, BufferedReader br) {
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
        } else error("syntax error");
    }

    public void start() {
        checkGuide(start);

        expr();
        match(Tag.EOF);
    }

    private void expr() {
        checkGuide(expr);

        term();
        exprp();
    }

    private void exprp() {
        checkGuide(exprp);

        switch (look.tag) {
            case '+':
                match('+');
                term();
                exprp();
                break;
            case '-':
                match('-');
                term();
                exprp();
                break;
            default:
                break;
        }
    }

    private void term() {
        checkGuide(term);

        fact();
        termp();
    }

    private void termp() {
        checkGuide(termp);

        switch (look.tag) {
            case '*':
                match('*');
                fact();
                termp();
                break;
            case '/':
                match('/');
                fact();
                termp();
                break;
            default:
                break;
        }
    }

    private void fact() {
        checkGuide(fact);

        switch (look.tag) {
            case '(':
                match('(');
                expr();
                match(')');
                break;
            case Tag.NUM:
                match(Tag.NUM);
                break;
            default:
                error("Expected '(' or number");
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "file"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.start();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}