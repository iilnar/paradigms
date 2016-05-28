package Evaluate.Expression;

import Evaluate.Exception.EvaluateException;

/**
 * Created by ����� on 30.03.2015.
 */
public class CheckedMultiply extends BinaryOperation {
    public CheckedMultiply(AnyExpression leftOperand, AnyExpression rightOperand) {
        super(leftOperand, rightOperand);
    }

    public int function(int left, int right) {
        if (right > 0 && (left > Integer.MAX_VALUE / right  || left < Integer.MIN_VALUE / right)) {
            throw new EvaluateException("Overflow");
        }
        if (right < -1 && (left > Integer.MIN_VALUE / right || left < Integer.MAX_VALUE / right) ) {
            throw new EvaluateException("Overflow");
        }
        if (right == -1 && left == Integer.MIN_VALUE) {
            throw new EvaluateException("Overflow");
        }
        return left * right;
    }
}
