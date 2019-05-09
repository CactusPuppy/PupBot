package usa.cactuspuppy.pupbot.api.game.minesweeper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter(AccessLevel.PACKAGE)
public final class Tile {
    private boolean isBomb = false;
    private int adjBombs = 0;

    public void incrAdjBombs() {
        adjBombs += 1;
    }
}
