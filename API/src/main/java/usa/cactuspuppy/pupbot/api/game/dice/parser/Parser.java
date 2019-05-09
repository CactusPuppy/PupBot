package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

import static usa.cactuspuppy.PVNBot.utils.dice.parser.Tokenizer.Token;

public class Parser {
    private LinkedList<Token> tokens;
    private Token lookahead;
    @Getter private String formula = "";

    public ExpressionNode parse(List<Token> tok) throws ParserException, EvalException {
        tokens = new LinkedList<>(tok);
        lookahead = tokens.pop();

        ExpressionNode expr = expression();
        if (lookahead.getTokenID() != Token.EPSILON) {
            throw new ParserException(String.format("Unexpected symbol %s found", lookahead));
        }

        return expr;
    }

    private void nextToken() {
        if (!tokens.isEmpty()) {
            lookahead = tokens.pop();
        } else {
            lookahead = new Token(Token.EPSILON, "");
        }
    }

    private ExpressionNode expression() {
        ExpressionNode expr = signedTerm();
        return sumOp(expr);
    }

    private ExpressionNode sumOp(ExpressionNode expr) {
        if (lookahead.getTokenID() == Token.ADD_SUB) {
            AdditionExpressionNode sum;
            if (expr.getType() == ExpressionNode.ADDITION_NODE) {
                sum = (AdditionExpressionNode) expr;
            } else {
                sum = new AdditionExpressionNode(expr, true);
            }

            boolean pos = lookahead.getSequence().equals("+");
            if (pos) {
                formula += " + ";
            } else {
                formula += " - ";
            }
            nextToken();
            ExpressionNode t = term();
            sum.add(t, pos);
            return sumOp(sum);
        }
        return expr;
    }

    private ExpressionNode signedTerm() {
        if (lookahead.getTokenID() == Token.ADD_SUB) {
            return getSignedExpressionNode(term());
        }
        return term();
    }

    private ExpressionNode getSignedExpressionNode(ExpressionNode term) {
        boolean pos = lookahead.getSequence().equals("+");
        if (!pos) formula += "-";
        nextToken();
        if (pos) {
            return term;
        } else {
            return new AdditionExpressionNode(term, false);
        }
    }

    private ExpressionNode term() {
        ExpressionNode f = factor();
        return termOp(f);
    }

    private ExpressionNode termOp(ExpressionNode expr) {
        if (lookahead.getTokenID() == Token.MUL_DIV) {
            MultiplicationExpressionNode prod;

            if (expr.getType() == ExpressionNode.MULTIPLICATION_NODE) {
                prod = (MultiplicationExpressionNode) expr;
            } else {
                prod = new MultiplicationExpressionNode(expr, true);
            }

            boolean pos = lookahead.getSequence().equals("*");
            if (pos) {
                formula += " * ";
            } else {
                formula += " / ";
            }
            nextToken();
            ExpressionNode f = signedFactor();
            prod.add(f, pos);

            return termOp(prod);
        }

        return expr;
    }

    private ExpressionNode signedFactor() {
        if (lookahead.getTokenID() == Token.ADD_SUB) {
            return getSignedExpressionNode(factor());
        }
        return factor();
    }

    private ExpressionNode factor() {
        ExpressionNode a = argument();
        return factorOp(a);
    }

    private ExpressionNode factorOp(ExpressionNode expr) {
        if (lookahead.getTokenID() == Token.POWER) {
            formula += "^";
            nextToken();
            ExpressionNode expon = signedFactor();

            return new ExponentiationExpressionNode(expr, expon);
        }

        return expr;
    }

    private ExpressionNode argument() {
        if (lookahead.getTokenID() == Token.OPEN_PAREN) {
            formula += "(";
            nextToken();
            ExpressionNode expr = expression();

            if (lookahead.getTokenID() != Token.CLOSE_PAREN) {
                throw new ParserException("Closing parentheses expected; got " + lookahead.getSequence() + " instead");
            }

            formula += ")";
            nextToken();
            return expr;
        } else {
            return value();
        }
    }

    private ExpressionNode value() {
        if (lookahead.getTokenID() == Token.NUMBER) {
            double value;
            try {
                value = Double.valueOf(lookahead.getSequence());
            } catch (NumberFormatException e) {
                throw new ParserException("Parsing issue on" + lookahead.getSequence());
            }
            ExpressionNode expr = new ConstantExpressionNode(value);
            formula += lookahead.getSequence();
            nextToken();
            return expr;
        } else if (lookahead.getTokenID() == Token.DICE) {
            DiceExpressionNode expr = new DiceExpressionNode(lookahead.getSequence());
            formula += expr.getStringRep();
            nextToken();
            return expr;
        }
        if (lookahead.getTokenID() == Token.EPSILON) { throw new ParserException("Unexpected end of input while parsing"); }
        throw new ParserException("Unexpected symbol " + lookahead.getSequence() + " while parsing");
    }

    public static class ParserException extends RuntimeException {
        ParserException(String message) {
            super(message);
        }
    }

    public static class EvalException extends RuntimeException {
        EvalException(String message) {
            super(message);
        }
    }
}
