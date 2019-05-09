package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes an input string for parsing
 */
public class Tokenizer {
    private static Map<Integer, TokenDef> tokenDefs = new LinkedHashMap<>();
    static {
        addTokenDef("(\\d+x)?(\\d+)?d(\\d+)(\\w+)?", Token.DICE); //dice rolls
        addTokenDef("\\(", Token.OPEN_PAREN); //open paren
        addTokenDef("\\)", Token.CLOSE_PAREN); //close paren
        addTokenDef("-?[0-9]+\\.?[0-9]*", Token.NUMBER); //numbers before add/sub to make sure we capture negative numbers
        addTokenDef("[+-]", Token.ADD_SUB); //add & sub
        addTokenDef("[*/]", Token.MUL_DIV); //mul & div
        addTokenDef("\\^", Token.POWER); //power
    }

    public static void addTokenDef(String regex, int token) {
        tokenDefs.put(token, new TokenDef(Pattern.compile("^(" + regex + ")"), token));
    }

    @AllArgsConstructor
    @Getter
    private static class TokenDef {
        private Pattern pattern;
        private int tokenID;
    }

    @AllArgsConstructor
    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class Token {
        public static final int EPSILON = 0;
        public static final int DICE = 1;
        public static final int OPEN_PAREN = 2;
        public static final int CLOSE_PAREN = 3;
        public static final int ADD_SUB = 4;
        public static final int MUL_DIV = 5;
        public static final int POWER = 6;
        public static final int NUMBER = 7;

        private int tokenID;
        private String sequence;
    }

    public static LinkedList<Token> tokenize(String s) throws TokenizerException {
        LinkedList<Token> tokens = new LinkedList<>();
        s = s.trim();
        while (!s.equals("")) {
            boolean found = false;
            for (TokenDef def : tokenDefs.values()) { //check all possible tokens for match
                Matcher m = def.getPattern().matcher(s);
                if (m.find()) {
                    if (def.getTokenID() == Token.NUMBER && (!tokens.isEmpty() && tokens.getLast().getTokenID() == Token.NUMBER)) { continue; } //Prevent subtraction from being called a negative number
                    found = true;
                    String seq = m.group().trim();
                    tokens.add(new Token(def.getTokenID(), seq));
                    s = m.replaceFirst("").trim(); //eat token
                    break;
                }
            }
            if (!found) throw new TokenizerException("Unexpected character in input: " + s);
        }
        return tokens;
    }

    public static class TokenizerException extends RuntimeException {
        public TokenizerException(String s) {
            super(s);
        }
    }
}
