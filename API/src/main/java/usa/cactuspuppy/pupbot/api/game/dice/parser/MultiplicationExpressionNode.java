package usa.cactuspuppy.pupbot.api.game.dice.parser;

public class MultiplicationExpressionNode extends SequenceExpressionNode {
    public MultiplicationExpressionNode() {
        super();
    }

    public MultiplicationExpressionNode(ExpressionNode a, boolean pos) {
        super(a, pos);
    }

    @Override
    public int getType() {
        return ExpressionNode.MULTIPLICATION_NODE;
    }

    @Override
    public double getValue() throws Parser.EvalException {
        double prod = 1.0;
        for (Term t : terms) {
            if (t.isPositive()) {
                if (t.getExpression().getValue() > 0) {
                    if (prod > Double.MAX_VALUE / t.getExpression().getValue()) {
                        throw new Parser.EvalException("Positive overflow while multiplying");
                    }
                } else if (t.getExpression().getValue() < 0) {
                    if (prod < Double.MIN_VALUE / t.getExpression().getValue()) {
                        throw new Parser.EvalException("Negative overflow while multiplying");
                    }
                }
                prod *= t.getExpression().getValue();
            } else {
                if (t.getExpression().getValue() == 0) {
                    throw new Parser.EvalException("Divide by zero error");
                }
                prod /= t.getExpression().getValue();
            }
        }
        return prod;
    }
}
