package usa.cactuspuppy.pupbot.api.game.dice.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Term {
    private boolean positive;
    private ExpressionNode expression;
}
