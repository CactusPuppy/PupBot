package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.NoArgsConstructor;

import java.util.LinkedList;

@NoArgsConstructor
public abstract class SequenceExpressionNode implements ExpressionNode {
    protected LinkedList<Term> terms = new LinkedList<>();

    public SequenceExpressionNode(ExpressionNode a, boolean positive) {
        add(a, positive);
    }

    public void add(ExpressionNode a, boolean pos) {
        terms.add(new Term(pos, a));
    }
}
