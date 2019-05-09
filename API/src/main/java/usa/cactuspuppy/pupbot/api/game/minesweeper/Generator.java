package usa.cactuspuppy.pupbot.api.game.minesweeper;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Random;
import java.util.StringJoiner;

@AllArgsConstructor
public final class Generator {
    private int width;
    private int height;
    private int numBombs;

    private static final int MAX_WIDTH = 50;
    private static final int MAX_HEIGHT = 24;
    private static final int MAX_BOMB_ATTEMPTS = 1000;

    /**
     * Generates a minesweeper board for play :D
     * @return a string representation of the board, or the failure messsage for creating that board
     * Failure message: %1$s - Author as mention
     */
    public String generateBoard() {
        if (width < 1) return failureMessage("Width must be at least 1");
        if (height < 1) return failureMessage("Height must be at least 1");
        if (numBombs < 1) return failureMessage("Number of bombs must be at least 1");
        if (width > MAX_WIDTH) return failureMessage("Width may not be greater than " + MAX_WIDTH);
        if (height > MAX_HEIGHT) return failureMessage("Height may not exceed " + MAX_HEIGHT);
        if (numBombs > width * height) return failureMessage("Cannot add more bombs than there are spaces on the board.");

        //Define first index to be horizontal index from left, and second index to be vertical index from top
        Tile[][] board = new Tile[width][height];
        for (int x = 0; x < board.length; x++) { //Initialize board array
            for (int y = 0; y < board[0].length; y++) {
                board[x][y] = new Tile();
            }
        }
        //Set bombs
        Random rng = new Random();
        for (int bombs = 0; bombs < numBombs; bombs++) {
            for (int attempt = 0; attempt < MAX_BOMB_ATTEMPTS; attempt++) {
                int x = rng.nextInt(width);
                int y = rng.nextInt(height);
                if (board[x][y].isBomb()) { continue; }
                setBomb(board, x, y);
                break;
            }
        }

        //Convert board to Message
        StringBuilder fullMessage = new StringBuilder();
        for (int y = 0; y < board[0].length; y++) {
            StringJoiner line = new StringJoiner(" ");
            for (int x = 0; x < board.length; x++) {
                line.add("||" + tileToEmoji(board[x][y]) + "||");
            }
            fullMessage.append(line.toString()).append("\n");
        }
        return fullMessage.toString();
    }

    /**
     * Helper function to set bomb
     * @param board board to place bomb in
     * @param x index from left of the bomb to place
     * @param y index from top of the bomb to place
     */
    private void setBomb(Tile[][] board, int x, int y) {
        assert 0 <= x && x < board.length;
        assert 0 <= y && y < board[0].length;
        board[x][y].setBomb(true);
        for (int x1 = x - 1; x1 <= x + 1; x1++) {
            for (int y1 = y - 1; y1 <= y + 1; y1++) {
                if (x1 >= 0 && y1 >= 0 &&
                        x1 < board.length && y1 < board[0].length &&
                        !(x1 == x && y1 == y)) {
                    board[x1][y1].incrAdjBombs();
                }
            }
        }
    }

    private String tileToEmoji(Tile tile) {
        if (tile.isBomb()) {
            return ":bomb:";
        }
        int level = tile.getAdjBombs();
        switch (level) {
            case 0:
                return ":zero:";
            case 1:
                return ":one:";
            case 2:
                return ":two:";
            case 3:
                return ":three:";
            case 4:
                return ":four:";
            case 5:
                return ":five:";
            case 6:
                return ":six:";
            case 7:
                return ":seven:";
            case 8:
                return ":eight:";
            default:
                return "ERROR";
        }
    }

    private String failureMessage(String reason) {
        return "%1$s Could not generate minesweeper board - " + reason;
    }

    public static void fullHandler(String[] args, MessageReceivedEvent e) {
        int width;
        int height;
        int bombs;
        if (args.length < 3) {
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Not enough arguments, need `<width> <height> <bombs>`").queue();
            return;
            //TODO: Check for difficulty enum
        }
        try {
            width = Integer.valueOf(args[0]);
            height = Integer.valueOf(args[1]);
            bombs = Integer.valueOf(args[2]);
        } catch (NumberFormatException e1) {
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Problem parsing arguments, make sure all arguments are integer numbers and try again.").queue();
            return;
        }
        String boardInfo = e.getMember().getAsMention() + "'s Minesweeper Game | " + bombs + " Mines | " + width + "x" + height + "\n";
        String calibrationInfo = "**CALIBRATION** - If the following line of :nine:'s takes up more than one line, this board will not work for you.\n";
        StringJoiner cRowBuilder = new StringJoiner(" ");
        for (int i = 0; i < width; i++) {
            cRowBuilder.add(":nine:");
        }
        String calibrationRow = cRowBuilder.toString() + "\n";
        String finalString = boardInfo + calibrationInfo + calibrationRow + String.format(
                new Generator(width, height, bombs).generateBoard(),
                e.getAuthor().getAsMention()
        );
        if (finalString.length() >= 2000) {
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Final minesweeper board is too big to show, try a smaller board.").queue();
            return;
        }
        e.getChannel().sendMessage(finalString).queue();
    }
}
