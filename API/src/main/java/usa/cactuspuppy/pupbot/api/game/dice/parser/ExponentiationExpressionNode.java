package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExponentiationExpressionNode implements ExpressionNode {
    private ExpressionNode base;
    private ExpressionNode exponent;

    @Override
    public int getType() {
        return ExpressionNode.EXPONENTIATION_NODE;
    }

    @Override
    public double getValue() {
        double value = Math.pow(base.getValue(), exponent.getValue());
        if (Double.isInfinite(value)) {
            throw new Parser.EvalException("Overflow while evaluating " + base.getValue() + "^" + exponent.getValue());
        } else if (Double.isNaN(value)) {
            throw new Parser.EvalException("NaN error while evaluating " + base.getValue() + "^" + exponent.getValue());
        }
        return value;
    }
}
