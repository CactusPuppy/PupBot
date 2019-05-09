package usa.cactuspuppy.pupbot.api.game.dice.parser;

public interface ExpressionNode {
    int DICE_NODE = 1;
    int CONSTANT_NODE = 2;
    int ADDITION_NODE = 3;
    int MULTIPLICATION_NODE = 4;
    int EXPONENTIATION_NODE = 5;

    int getType();
    double getValue();
}
