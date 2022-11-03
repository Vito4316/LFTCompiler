import java.io.*;
import java.util.*;

public class Lexer {
    HashMap<String, Word> tokenDict;
    public static int line = 1;
    private char peek = ' ';

    private void fillDict() {
        tokenDict = new HashMap<>();
        tokenDict.put("assign", Word.assign);
        tokenDict.put("to", Word.to);
        tokenDict.put("conditional", Word.conditional);
        tokenDict.put("option", Word.option);
        tokenDict.put("do", Word.dotok);
        tokenDict.put("else", Word.elsetok);
        tokenDict.put("while", Word.whiletok);
        tokenDict.put("begin", Word.begin);
        tokenDict.put("end", Word.end);
        tokenDict.put("print", Word.print);
        tokenDict.put("read", Word.read);
    }

    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }

        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;
            case '(':
                peek = ' ';
                return Token.lpt;
            case ')':
                peek = ' ';
                return Token.rpt;
            case '[':
                peek = ' ';
                return Token.lpq;
            case ']':
                peek = ' ';
                return Token.rpq;
            case '{':
                peek = ' ';
                return Token.lpg;
            case '}':
                peek = ' ';
                return Token.rpg;
            case '+':
                peek = ' ';
                return Token.plus;
            case '-':
                peek = ' ';
                return Token.minus;
            case '*':
                peek = ' ';
                return Token.mult;
            case '/':
                readch(br);
                if(peek == '/') return skipInlineComment(br);
                else if(peek == '*') return skipMultilineComment(br);
                else return Token.div;
            case ';':
                peek = ' ';
                return Token.semicolon;
            case ',':
                peek = ' ';
                return Token.comma;

            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character" + " after & : "  + peek );
                    return null;
                }

            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character" + " after | : "  + peek );
                    return null;
                }

            case '<':
                readch(br);
                if(peek == '=') {
                    peek = ' ';
                    return Word.le;
                }
                else if(peek == '>') {
                    peek = ' ';
                    return Word.ne;
                }
                return Word.lt;

            case '>':
                readch(br);
                if(peek == '=') {
                    peek = ' ';
                    return Word.ge;
                }
                return Word.gt;

            case '=':
                readch(br);
                if(peek == '=') {
                    peek = ' ';
                    return Word.eq;
                }
                else {
                    System.err.println("Erroneous character after = :" + peek);
                    return null;
                }

            case (char)-1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek) || peek == '_') {
                    return identifierTok(br);

                } else if (Character.isDigit(peek)) {
                    return numberTok(br);

                } else {
                    System.err.println("Erroneous character: " + peek );
                    return null;
                }
        }
    }

    private NumberTok numberTok(BufferedReader br) {
        int num = 0;
        int state = 0;
        while(state != 3 && state != -1) {
            switch (state) {
                case 0 -> state = AutomGetNumState(1, 2, -1);
                case 1 -> state = AutomGetNumState(3, 3, 3);
                case 2 -> state = AutomGetNumState(2, 2, 3);
            }
            if(state != 3 && state != 4) {
                num *=10;
                num += peek - '0';
                readch(br);
            }
        }

        if(state == 3)
            return new NumberTok(Tag.NUM, num);
        System.err.println("Erroneous number at line: " + line);
        return null;
    }

    private int AutomGetNumState(int is0, int isNum, int isOther) {
        if(peek == '0')
            return is0;
        if(peek > '0' && peek <= '9')
            return isNum;
        return isOther;
    }

    private Word identifierTok(BufferedReader br) {
        StringBuilder wordBuilder = new StringBuilder();

        int state = 0;
        while(state != 3 && state != -1) {
            switch (state) {
                case 0 -> state = AutomGetIdState(-1, 1, -1);
                case 1 -> state = AutomGetIdState(2, 1, -1);
                case 2 -> state = AutomGetIdState(2, 2, 3);
            }
            if(state != 3) {
                wordBuilder.append(peek);
                readch(br);
            }
        }

        String word = wordBuilder.toString();

        if(state == 3) {
            if (tokenDict.containsKey(word))
                return tokenDict.get(word);
            return new Word(Tag.ID, word);
        }
        System.err.println("Erroneous identifier: " + wordBuilder);
        return null;
    }

    private int AutomGetIdState(int isN, int is_, int isOther) {
        if(Character.isDigit(peek)) return isN;
        if(Character.isLetter(peek)) return 2;
        if(peek == '_') return is_;
        else return isOther;
    }

    private Token skipMultilineComment(BufferedReader br) {
        if(automIsComment(br)) {
            peek = ' ';
            return lexical_scan(br);
        }
        else {
            System.err.println("Error, not ending comment");
            return null;
        }
    }

    private Token skipInlineComment(BufferedReader br) {
        while (peek != '\n' && peek != '\0' && peek != (char)-1) {readch(br);}
        return lexical_scan(br);
    }

    private boolean automIsComment(BufferedReader br) {
        int state = 2;
        while(state != 4 && state != -1) {
            readch(br);
            switch (state) {
                case 0 ->state = automGetCommentState(1, -1, -1);
                case 1 ->state = automGetCommentState(-1 , 2, -1);
                case 2 ->state = automGetCommentState(2, 3, 2);
                case 3 ->state = automGetCommentState(4, 3, 2);
            }
        }
        return state == 4;
    }

    private int automGetCommentState(int slash, int star, int a) {
        if(peek == '/') return slash;
        if(peek == '*') return star;
        if(peek == (char)-1) return -1;
        else return a;
    }
    public Lexer() {
        fillDict();
    }
}