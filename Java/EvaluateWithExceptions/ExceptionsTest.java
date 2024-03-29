package Evaluate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongBinaryOperator;

import static Evaluate.Util.*;
import Evaluate.Expression.*;
import Evaluate.Parser.CheckedParser;
import Evaluate.Parser.Parser;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class ExceptionsTest extends ParserTest {
    private final static int D = 5;
    private final static List<Integer> VALUES = new ArrayList<>();
    private final char[] CHARS = "AZ-+*%()[]".toCharArray();

    static {
        addRange(VALUES, D, Integer.MIN_VALUE + D);
        addRange(VALUES, D, Integer.MIN_VALUE / 2);
        addRange(VALUES, D, (int) -Math.sqrt(Integer.MAX_VALUE));
        addRange(VALUES, D, 0);
        addRange(VALUES, D, (int) Math.sqrt(Integer.MAX_VALUE));
        addRange(VALUES, D, Integer.MAX_VALUE / 2);
        addRange(VALUES, D, Integer.MAX_VALUE - D);
    }

    private int subtests = 0;

    public static void main(final String[] args) {
        checkAssert(ExceptionsTest.class);
        new ExceptionsTest().test();
    }

    @Override
    protected void test() {
        final Variable vx = new Variable("x");
        final Variable vy = new Variable("y");

        check((a, b) -> a + b, "+", new CheckedAdd(vx, vy));
        check((a, b) -> a - b, "-", new CheckedSubtract(vx, vy));
        check((a, b) -> a * b, "*", new CheckedMultiply(vx, vy));
        check((a, b) -> b == 0 ? Long.MAX_VALUE : a / b, "/", new CheckedDivide(vx, vy));
        check((a, b) -> -b, "<- ignore first argument, unary -", new CheckedNegate(vy));

        System.out.println("OK, " + subtests + " subtests");

        super.test();
    }

    private void check(final LongBinaryOperator f, final String op, final TripleExpression expression) {
        for (final int a : VALUES) {
            for (final int b : VALUES) {
                final long expected = f.applyAsLong(a, b);
                try {
                    final int actual = expression.evaluate(a, b, 0);
                    assert actual == expected : a + " " + op + " " + b + " == " + actual;
                } catch (final Exception e) {
                    if (Integer.MIN_VALUE <= expected && expected <= Integer.MAX_VALUE) {
                        throw new AssertionError("Unexpected error in " + a + " " + op + " " + b, e);
                    }
                }
                assert ++subtests > 0;
            }
        }
    }

    @Override
    protected Either<Reason, Integer> lift(final Long value) {
        return value == null ? Either.left(Reason.DBZ)
                : value < Integer.MIN_VALUE || Integer.MAX_VALUE < value ? Either.left(Reason.OVERFLOW)
                : Either.right(value.intValue());
    }


    @Override
    protected TripleExpression parse(final String expression) {
        final Parser parser = new CheckedParser();
        if (expression.length() > 10) {
            loop: for (final char ch : CHARS) {
                for (int i = 0; i < 10; i++) {
                    final int index = 1 + randomInt(expression.length() - 2);
                    final char c = expression.charAt(index);
                    if ("-( *".indexOf(c) < 0 && !Character.isLetterOrDigit(c)) {
                        final String input = expression.substring(0, index) + ch + expression.substring(index);
                        try {
                            parser.parse(input);
                            throw new AssertionError("Parsing error expected for " + expression.substring(0, index) + "<ERROR_INSERTED -->" + ch + expression.substring(index));
                        } catch (final Exception e) {
                            // Ok
                        }
                        continue loop;
                    }
                }
            }
        }
        try {
            return parser.parse(expression);
        } catch (final Exception e) {
            throw new AssertionError("Parser failed", e);
        }
    }
}