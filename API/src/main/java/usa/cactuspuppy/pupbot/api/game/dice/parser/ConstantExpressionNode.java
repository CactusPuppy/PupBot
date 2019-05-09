package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConstantExpressionNode implements ExpressionNode {
    private double value;

    @Override
    public int getType() {
        return ExpressionNode.CONSTANT_NODE;
    }

    @Override
    public double getValue() {
        return value;
    }
}
