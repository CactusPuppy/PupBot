package usa.cactuspuppy.pupbot.api.game.dice.parser;

public class AdditionExpressionNode extends SequenceExpressionNode {
    public AdditionExpressionNode() {
        super();
    }

    public AdditionExpressionNode(ExpressionNode a, boolean pos) {
        super(a, pos);
    }

    @Override
    public int getType() {
        return ExpressionNode.ADDITION_NODE;
    }

    @Override
    public double getValue() throws Parser.EvalException {
        double sum = 0.0;
        for (Term t : terms) {
            double val = t.getExpression().getValue();
            if (!t.isPositive()) {
                val = val * -1;
            }
            if (val > 0 && sum > Double.MAX_VALUE - val) {
                throw new Parser.EvalException("Positive overflow while summing");
            } else if (val < 0 && sum < Double.MIN_VALUE + val) {
                throw new Parser.EvalException("Negative overflow while summing");
            }
            sum += val;
        }
        return sum;
    }
}
