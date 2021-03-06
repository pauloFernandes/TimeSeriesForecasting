package geneticProgramming.functions.function.logic;

import geneticProgramming.functions.Node;
import geneticProgramming.functions.function.BinaryNode;
import geneticProgramming.functions.terminal.*;

/**
 *
 * GreaterThan operator.
 *
 * This is an implementation of GreaterThan operator.
 *
 * This class must compare two sentences.
 * This two parameters must be instances of boolean classes.
 *
 * This class will evaluate boolean results.
 *
 * @author Paulo Fernandes
 *
 * Created with IntelliJ IDEA.
 * User: Paulo
 * Date: 20/06/13
 * Time: 13:40
 */
public class GreaterThan extends BinaryNode
{

    public static final String GREATER_THEN_SYMBOL = ">";

    public GreaterThan(Node left, Node right)
    {
        super(left, right, GREATER_THEN_SYMBOL);
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
        final double returnValue = this.left.evaluate(programParameters) > this.right.evaluate(programParameters) 
                                    ? 1 : 0;
        return (Double.isNaN(returnValue) || Double.isInfinite(returnValue)) ? Node.BAD_FITNESS_VALUE : returnValue;
    }

    /**
     * Returns which type this node represents.
     *
     * @return returns the type of this node.
     */
    @Override
    public int getType() {
        return Node.BOOLEAN_NODE;
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

        if (simplifiedLeft.equals(simplifiedRight)) {
            return new False();
        }

        if (simplifiedLeft instanceof Constant && simplifiedRight instanceof Constant) {
            if (simplifiedLeft.evaluate(NO_ARGS) == simplifiedRight.evaluate(NO_ARGS)) {
                return new False();
            }

            if (simplifiedLeft.evaluate(NO_ARGS) > simplifiedRight.evaluate(NO_ARGS)) {
                return new True();
            } else {
                return new False();
            }
        }

        if (simplifiedLeft != this.left || simplifiedRight != this.right) {
            return new GreaterThan(simplifiedLeft, simplifiedRight);
        } else {
            return this;
        }
    }

}
