package usa.cactuspuppy.pupbot.api.game.dice;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang.StringUtils;
import usa.cactuspuppy.PVNBot.utils.dice.parser.ExpressionNode;
import usa.cactuspuppy.PVNBot.utils.dice.parser.Parser;
import usa.cactuspuppy.PVNBot.utils.dice.parser.Tokenizer;
import usa.cactuspuppy.PVNBot.utils.discord.Messaging;

import java.awt.*;
import java.util.StringJoiner;

public class DiceWrapper {
    private static final int MAX_FORMULA_LENGTH = 100;

    public static void command(MessageReceivedEvent e, String[] args) {
        if (!e.getChannel().getName().equals("dice-rolling")) {
            Messaging.sendSnapMsg(String.format("%s PupBot has been instructed to ignore dice roll commands in this channel to avoid spam. Sorry :|", e.getAuthor().getAsMention()), e.getChannel());
            e.getMessage().delete().queue();
            return;
        }
        if (args.length == 0) {
            args = new String[]{"1d20"};
        }
        DiceWrapper.perform(args, e);
    }

    public static void perform(String[] args, MessageReceivedEvent e) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String s : args) {
            joiner.add(s);
        }
        String noSpace = joiner.toString();

        Parser parser = new Parser();
        ExpressionNode result;
        double resultValue;
        try {
            result = parser.parse(Tokenizer.tokenize(noSpace));
            resultValue = result.getValue();
        } catch (Parser.ParserException | Parser.EvalException | Tokenizer.TokenizerException e1) {
            parseProblem(e, e1.getMessage());
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0, 193, 255));
        eb.setTitle(String.format("%s's Roll", e.getMember().getEffectiveName()));
        String formula = parser.getFormula();
        if (StringUtils.countMatches(formula, " ") >= MAX_FORMULA_LENGTH) {
            formula = formula.substring(0, StringUtils.ordinalIndexOf(formula, " ", MAX_FORMULA_LENGTH)) + "...";
        }
        eb.addField("**Calculation**", formula, false);
        eb.addField("**Result**", String.valueOf(resultValue), false);
        e.getChannel().sendMessage(eb.build()).queue();
    }

    private static void parseProblem(MessageReceivedEvent e, String reason) {
        e.getChannel().sendMessage(String.format("Couldn't roll the dice for you %s. Reason: %s", e.getAuthor().getAsMention(), reason)).queue();
    }
}
