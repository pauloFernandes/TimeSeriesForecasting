package geneticProgramming.functions.function.basic;

import geneticProgramming.functions.function.BinaryNode;
import geneticProgramming.functions.terminal.Constant;
import geneticProgramming.functions.Node;

/**
 * Addition operator.
 *
 * This is an implementation of Addition operator.
 *
 * This class must sum two values. This two parameters must be  arithmetic classes' instances.
 *
 * This class will evaluate numeric results.
 *
 *
 * @author Paulo Fernandes
 *
 * Created with IntelliJ IDEA.
 * User: Paulo
 * Date: 18/06/13
 * Time: 00:43
 */
public class Addition extends BinaryNode
{

    public static final String ADDITION_SYMBOL = "+";

    public Addition(Node left, Node right)
    {
        super(left, right, Addition.ADDITION_SYMBOL);
    }

    /**
     * Recursively evaluates the (sub-)tree represented by this node (including any child nodes) and return the fitness
     * value of this.
     *
     * @param programParameters Program parameters, used by this node or its children.
     * @return Returns a double value representing this node's fitness value.
     */
    @Override
    public double evaluate(double[] programParameters)
    {
        final double returnValue = this.left.evaluate(programParameters) + this.right.evaluate(programParameters);
        return (Double.isNaN(returnValue) || Double.isInfinite(returnValue)) ? Node.BAD_FITNESS_VALUE : returnValue;
    }

    /**
     * Reduces this program tree to this simplest equivalent representation form.
     * @return A tree representing this one simplified, or this, if it cannot be done.
     */
    @Override
    public Node simplify()
    {
        Node simplifiedLeft  = this.left.simplify();
        Node simplifiedRight = this.right.simplify();

        // Adding zero is pointless, the expression can be reduced to its other argument.
        if (simplifiedRight instanceof Constant && simplifiedRight.evaluate(Addition.NO_ARGS) == 0) {
            return simplifiedLeft;
        }

        if (simplifiedLeft instanceof Constant && simplifiedLeft.evaluate(Addition.NO_ARGS) == 0) {
            return simplifiedRight;
        }

        // If the two arguments are constants, it can be reduced to its final value, since constant values will never
        // change.
        if (simplifiedLeft instanceof Constant && simplifiedRight instanceof Constant) {
            return new Constant(simplifiedLeft.evaluate(Addition.NO_ARGS) + simplifiedRight.evaluate(Addition.NO_ARGS));
        } else if (simplifiedLeft != this.left || simplifiedRight != this.right) {
            return new Addition(simplifiedLeft, simplifiedRight);
        } else {
            return this;
        }
    }
}
