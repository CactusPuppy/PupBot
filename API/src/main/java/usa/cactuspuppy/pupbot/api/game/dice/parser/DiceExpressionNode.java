package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.Getter;
import usa.cactuspuppy.PVNBot.utils.dice.DiceRoller;

public class DiceExpressionNode implements ExpressionNode {
    /**
     * How this dice roll should be represented in the formula
     */
    @Getter
    private String stringRep;
    private double value;

    public DiceExpressionNode(String stringRep) {
        this.stringRep = stringRep;
        setValue();
    }

    @Override
    public int getType() {
        return ExpressionNode.DICE_NODE;
    }

    @Override
    public double getValue() {
        return value;
    }

    public void setValue() {
        DiceRoller.RollResult result = DiceRoller.parseRoll(stringRep);
        if (!result.isSuccess()) {
            throw new Parser.EvalException(result.getReason());
        }
        value = result.getResult();
        stringRep = "[" + result.getFormula() + "]";
}
}
