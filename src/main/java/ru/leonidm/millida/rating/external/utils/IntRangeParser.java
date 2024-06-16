package ru.leonidm.millida.rating.external.utils;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Supcheg
 */
public final class IntRangeParser {

    @Language("RegExp")
    public static final String PATTERN = "^[0-9]+(?:-[0-9]+)?(?:,[0-9]+(?:-[0-9]+)?)*$";

    private static final Pattern IS_VALID = Pattern.compile(PATTERN);
    private static final Pattern RE_NEXT_VAL = Pattern.compile("([0-9]+)(?:-([0-9]+))?(?:,|$)");

    private IntRangeParser() {

    }

    @NotNull
    @Contract("_ -> new")
    public static IntStream parseIntRange(@org.intellij.lang.annotations.Pattern(IntRangeParser.PATTERN) @NotNull String input) {
        if (!IS_VALID.matcher(input).matches()) {
            throw new IllegalArgumentException(String.format("'%s' doesn't matches int range format", input));
        }

        Matcher matcher = RE_NEXT_VAL.matcher(input);
        IntStream stream = IntStream.empty();
        while (matcher.find()) {
            String rawStart = matcher.group(1);
            int start = Integer.parseInt(rawStart);

            String rawEnd = matcher.group(2);
            if (rawEnd == null) {
                stream = IntStream.concat(stream, IntStream.of(start));
            } else {
                int end = Integer.parseInt(rawEnd);

                if (end < start) {
                    throw new IllegalArgumentException(String.format("End: %d is less than start: %d", end, start));
                }
                stream = IntStream.concat(stream, IntStream.rangeClosed(start, end));
            }
        }

        return stream;

    }
}
