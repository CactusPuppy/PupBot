package usa.cactuspuppy.pupbot.api.game.dice;

import lombok.*;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class with ability to parse and perform rolls of format AdX with potential modifiers
 * @author CactusPuppy
 */
public final class DiceRoller {
    private static final int MAX_ROLLS = 100;
    private static final int MAX_SIDES = 9001;
    private static final int MAX_ATTEMPTS = 10000;
    private static final int MAX_REPEATS = 50;
    private static Map<Character, RollModifier> rollModifiers = new HashMap<>();
    @Setter private static Random rng = new Random();
    static {
        addModifier("k(\\d+)", 'k');
        addModifier("r(\\d+)", 'r');
        addModifier("D(\\d+)", 'D');
        addModifier("v", 'v');
    }

    @AllArgsConstructor
    @Getter
    public static class Rolls {
        private long result;
        private List<Integer> rolls;
        private List<Integer> drops;

        @Override
        public String toString() {
            StringJoiner rolls = new StringJoiner(" + ");
            LinkedList<Integer> dropsCopy = new LinkedList<>(drops);
            for (int r : this.rolls) {
                if (dropsCopy.contains(r)) {
                    rolls.add("~~" + r + "~~");
                    dropsCopy.removeFirstOccurrence(r);
                } else {
                    rolls.add("**" + r + "**");
                }
            }
            return rolls.toString();
        }
    }

    @NoArgsConstructor
    @Getter
    @Setter(AccessLevel.PRIVATE)
    public static class RollResult {
        private boolean success;
        private String reason;
        private int sides;
        private int rolls;
        private double result;
        private String formula;
    }

    @AllArgsConstructor
    @Getter
    private static class RollModifier {
        private Pattern pattern;
        private char mod;
    }

    private static void addModifier(String regex, char mod) {
        rollModifiers.put(mod, new RollModifier(Pattern.compile("^(" + regex + ")"), mod));
    }

    /**
     * Performs a AdB format roll with potential modifiers Xn<br>
     *     <p>
     *         Modifiers:<br>
     *         k - keep highest n rolls<br>
     *         D - drop n lowest rolls<br>
     *         r - reroll every die which yields n<br>
     *         v - show dropped rolls<br>
     *     </p>
     *
     * @param command Roll formula to parse
     * @param keepDrops Whether to include dropped rolls in the returned formula
     * @return Map containing:<br>
     *     "success" - "true" if calculation was successful, "false" otherwise<br>
     *     "reason" - If success if false, reason why parsing unsuccessful.<br>
     *     "sides" - Number of sides on dice rolled<br>
     *     "rolls" - Number of dice rolled<br>
     *     "result" - The final total of the formula<br>
     *     "formula" - All rolls concatenated with " + ", dropped rolls braced by "~~"<br>
     */
    public static RollResult parseRoll(String command, boolean keepDrops) {
        RollResult results = new RollResult();
        int rolls = 1;
        int sides;
        int drop = 0;
        int repeat = 1;
        List<Integer> rerolls = new ArrayList<>();

        //Begin parsing
        Pattern p = Pattern.compile("(\\d+x)?(\\d+)?d(\\d+)(\\w+)?");
        Matcher m = p.matcher(command);
        //Check format
        if (!m.matches()) {
            results.setSuccess(false);
            results.setReason("Incorrect formatting");
            return results;
        }
        //Get repetitions
        if (m.group(1) != null) {
            String repetitions = m.group(1).substring(0, m.group(1).length() - 1);
            try {
                repeat = stringToInt(repetitions);
                if (repeat < 1) {
                    results.setSuccess(false);
                    results.setReason("Number of repeats must be positive");
                    return results;
                } else if (repeat > MAX_REPEATS) {
                    results.setSuccess(false);
                    results.setReason("Number of repeats may not exceed " + MAX_REPEATS);
                }
            } catch (NumberFormatException e) {
                results.setSuccess(false);
                results.setReason(e.getMessage());
                return results;
            }
        }
        //Get rolls
        if (m.group(2) != null) {
            try {
                rolls = stringToInt(m.group(2));
                if (rolls <= 0) {
                    results.setSuccess(false);
                    results.setReason("Number of rolls must be positive");
                    return results;
                } else if (rolls > MAX_ROLLS) {
                    results.setSuccess(false);
                    results.setReason("Number of rolls may not exceed " + MAX_ROLLS);
                    return results;
                }
            } catch (NumberFormatException e) {
                results.setSuccess(false);
                results.setReason(e.getMessage());
                return results;
            }
        }
        //Get sides
        try {
            sides = stringToInt(m.group(3));
            if (sides <= 0) {
                results.setSuccess(false);
                results.setReason("Number of sides must be positive");
                return results;
            } else if (sides > MAX_SIDES) {
                results.setSuccess(false);
                results.setReason("Number of rolls may not exceed " + MAX_SIDES);
                return results;
            }
        } catch (NumberFormatException e) {
            results.setSuccess(false);
            results.setReason(e.getMessage());
            return results;
        }
        //Parse modifiers
        if (m.group(4) != null) {
            String modifiers = m.group(4);
            while (!modifiers.equals("")) {
                boolean match = false;
                //Attempt to match all modifiers with regex
                for (RollModifier r : rollModifiers.values()) {
                    Matcher matcher = r.getPattern().matcher(modifiers);
                    if (matcher.find()) { //found a match
                        try {
                            match = true;
                            switch (r.getMod()) {
                                case 'k': {
                                    int temp = stringToInt(matcher.group().substring(1));
                                    if (temp > rolls) {
                                        throw new NumberFormatException("Cannot keep more rolls than dice being rolled");
                                    }
                                    drop = rolls - temp;
                                    break;
                                }
                                case 'D': {
                                    int temp = stringToInt(matcher.group().substring(1));
                                    if (temp > rolls) {
                                        throw new NumberFormatException("Cannot drop more rolls than dice being rolled");
                                    }
                                    drop = temp;
                                    break;
                                }
                                case 'r': {
                                    int temp = stringToInt(matcher.group().substring(1));
                                    if (temp > sides) {
                                        throw new NumberFormatException("Specified reroll value greater than number of sides");
                                    }
                                    rerolls.add(temp);
                                    break;
                                }
                                case 'v': {
                                    keepDrops = true;
                                    break;
                                }
                                default: throw new NumberFormatException("Unknown modifier " + r.getMod());
                            }
                            modifiers = matcher.replaceFirst("").trim(); //eat modifier
                            break;
                        } catch (NumberFormatException e) { //Couldn't extract an int from the trailing numbers
                            results.setSuccess(false);
                            results.setReason(e.getMessage());
                            return results;
                        }
                    }
                }
                if (!match) {
                    results.setSuccess(false);
                    results.setReason("Issue parsing modifiers at: " + modifiers);
                    return results;
                }
            }
        }
        //ROLL FOR INITIATIVE
        Rolls rollResult;
        long sumResult = 0;
        StringJoiner formulaBuilder = new StringJoiner(") + (", "(", ")");
        for (int i = 0; i < repeat; i++) {
            try {
                rollResult = roll(rolls, sides, rerolls, drop, keepDrops);
                if (sumResult > Long.MAX_VALUE - rollResult.getResult()) {
                    results.setSuccess(false);
                    results.setReason("Overflow while repeating roll");
                    return results;
                }
                sumResult += rollResult.getResult();
                formulaBuilder.add(rollResult.toString());
            } catch (Exception e) {
                results.setSuccess(false);
                results.setReason(e.getMessage());
                return results;
            }
        }
        results.setSuccess(true);
        results.setSides(sides);
        results.setRolls(rolls);
        results.setResult(sumResult);
        results.setFormula(formulaBuilder.toString());
        return results;
    }

    /**
     * Overloaded single roll command
     * @param command roll command
     * @return the resulting rolls without dropped rolls
     */
    public static RollResult parseRoll(String command) {
        return parseRoll(command, false);
    }

    /**
     * Generates rolls of dice, with option to keep a certain number and rerolling a subset of possible values
     * @param rolls number of rolls
     * @param sides number of sides on each die
     * @param rerolls If a roll generates any of these values, reroll. (Null or Empty List disables)
     * @param drop How many lowest rolls to drop
     * @return Rolls representing the rolls
     * @throws IllegalArgumentException Thrown if any argument is non-sensical (i.e. reroll every possible value)
     * @throws RuntimeException If the roller is unable to get rolls in a reasonable number of iterations
     */
    public static Rolls roll(int rolls, int sides, List<Integer> rerolls, int drop, boolean keepDrops) throws RuntimeException {
        if (rerolls == null) rerolls = new ArrayList<>();
        //Sanity checks
        if (rolls <= 0) throw new IllegalArgumentException("Number of rolls must be positive");
        if (sides <= 0) throw new IllegalArgumentException("Number of sides must be positive");
        if (drop >= rolls) throw new IllegalArgumentException("Number of drops must be less than number of rolls");
        //Filter passed rerolls
        if (rerolls.size() > 0) rerolls = rerolls.stream().distinct().filter(i -> isPossible(i, sides)).collect(Collectors.toList());
        if (rerolls.size() == sides) throw new IllegalArgumentException("Cannot reroll on every possible roll");
        int cRolls = 0; //completed rolls counter
        LinkedList<Integer> rolled = new LinkedList<>();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int roll = rng.nextInt(sides) + 1;
            if (rerolls.contains(roll)) continue; //ignore this roll
            rolled.add(roll);
            cRolls++;
            if (cRolls == rolls) break;
            if (i == MAX_ATTEMPTS - 1) throw new RuntimeException("Timeout while rolling dice");
        }
        int sum = rolled.stream().mapToInt(Integer::intValue).sum();
        if (drop == 0) {
            return new Rolls(sum, rolled, new ArrayList<>(0));
        }
        LinkedList<Integer> keep = new LinkedList<>(rolled);
        ArrayList<Integer> dropped = new ArrayList<>(drop);
        for (int i = 0; i < drop; i++) {
            int min = Collections.min(keep);
            dropped.add(min);
            keep.removeFirstOccurrence(min);
        }
        sum = keep.stream().mapToInt(Integer::intValue).sum();
        return new Rolls(sum, (keepDrops ? rolled : keep), (keepDrops ? dropped : new ArrayList<>(0)));
    }

    /**
     * Checks if the given value is possible to roll on a die with the given # of sides
     * @param value value to check
     * @param sides number of sides on the die
     * @return whether value can be rolled on this die
     * @throws NumberFormatException thrown if the string could not be converted
     */
    private static boolean isPossible(int value, int sides) {
        return value >= 1 && value <= sides;
    }

    public static int stringToInt(String s) throws NumberFormatException {
        try {
            int i = Integer.parseInt(s);
            return i;
        } catch (NumberFormatException e) {
            try {
                new BigInteger(s);
            } catch (Exception e1) {
                throw e;
            }
            throw new NumberFormatException("String exceeds integer bounds");
        }
    }
}
