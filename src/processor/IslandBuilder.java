package processor;

import geneticProgramming.configuration.GPConfiguration;
import geneticProgramming.configuration.IslandConfiguration;
import geneticProgramming.functions.Node;
import geneticProgramming.functions.function.Simplification;
import geneticProgramming.geneticOperators.*;
import model.TimeNode;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Island Builder
 *
 * This class is responsible to create all instances of islands required to run the GP Machine.
 * The number of islands and their configuration are set by an IslandConfiguration object.
 *
 * Created with IntelliJ IDEA.
 * User: paulo
 * Date: 11/08/13
 * Time: 01:13
 */
public class IslandBuilder
{

    /**
     * This method is the external interface of this class. It will receive as parameter the list of configurations,
     * where each element of this list represents an island to be created. It will also receive as parameter an array
     * with the training data of this system, because this is a required object for some of the components of an island
     * (the fitness evaluator).
     *
     * @param configurations List of objects that shows how an island must looks like.
     * @param data           Training data. This object is used by the fitness evaluator class.
     *
     * @return Return all islands that will be used in the GP Machine.
     * @throws Exception
     */
    public static List<EvolutionEngine<Node>> build(ArrayList<IslandConfiguration> configurations,
                                                    GPConfiguration gpConfiguration,
                                                    ArrayList<TimeNode> data) throws Exception
    {
        List<EvolutionEngine<Node>> islands = new ArrayList<EvolutionEngine<Node>>();
        for (IslandConfiguration configuration : configurations) {
            System.out.println("creating a new island");
            islands.add(createSingleIsland(configuration, gpConfiguration, data));
        }

        return islands;
    }

    /**
     * Creates a single instance of an Island. This method is an auxiliary for the main method.
     *
     * @param islandConfiguration  Object that shows how an island must looks like.
     * @param data           Training data. This object is used by the fitness evaluator class.
     *
     * @return Return an island that will be used in the GP Machine.
     * @throws Exception
     */
    private static EvolutionEngine<Node> createSingleIsland(IslandConfiguration islandConfiguration,
                                                            GPConfiguration gpConfiguration,
                                                            ArrayList<TimeNode> data) throws Exception
    {
        TreeFactory factory                        = getCandidateFactory(islandConfiguration, gpConfiguration);
        EvolutionPipeline<Node> operators          = getEvolutionaryOperators(islandConfiguration, gpConfiguration, factory);
        FitnessEvaluator<Node> fitnessEvaluator    = getFitnessEvaluator(islandConfiguration, gpConfiguration, data);
        SelectionStrategy selectionStrategy        = SelectionStrategyFactory.factory(islandConfiguration);

        return new GenerationalEvolutionEngine<Node>(
            factory, operators, fitnessEvaluator, selectionStrategy, new MersenneTwisterRNG()
        );
    }

    /**
     * Returns the fitness evaluator, prepared as it was configured in the Island Configuration File.
     *
     * @param configuration Object that shows how an island must looks like.
     * @param data          Training data. This object is used by the fitness evaluator class.
     *
     * @return Returns an instance of TimeSeriesEvaluator.
     */
    private static TimeSeriesEvaluator getFitnessEvaluator(IslandConfiguration configuration,
                                                           GPConfiguration gpConfiguration,
                                                           ArrayList<TimeNode> data)
    {
        return new TimeSeriesEvaluator(
            data, gpConfiguration.getWindowSize(), configuration.getFitnessStrategy(),
            gpConfiguration.isFitnessNatural()
        );
    }

    /**
     * Returns a set of operators that will be used in the GP Machine. Some of the genetic operators provided in this
     * project can be out of the execution, if the configurations tell so.
     *
     * @param configuration Object that shows how an island must looks like.
     * @param factory       Candidates factory instance, required for some of the operators.
     *
     * @return Return a set of operators.
     */
    private static EvolutionPipeline<Node> getEvolutionaryOperators(IslandConfiguration configuration,
                                                                    GPConfiguration gpConfiguration,
                                                                    TreeFactory factory)
    {
        List<EvolutionaryOperator<Node>> operators = new ArrayList<EvolutionaryOperator<Node>>();

        if (configuration.isEnableMutation()) {
            operators.add(new TreeMutation(factory, new Probability(configuration.getMutationProbability())));
        }

        if (configuration.isEnableCrossover()) {
            operators.add(new TreeCrossover(configuration.getMaxTreeDepth()));
        }

        if (configuration.isEnablePlague()) {
            operators.add(
                new Plague(
                    factory, configuration.getSurvivorPlagueProbability(),
                    configuration.getTotalOfPlagueSpreads(),
                    configuration.getGenerationsCountBeforePlague(),
                    gpConfiguration.isFitnessNatural()
                )
            );
        }

        operators.add(new Simplification(factory));

        return new EvolutionPipeline<Node>(operators);
    }

    /**
     * Creates an instance of a candidate factory, set as the configuration provided.
     *
     * @param configuration Object that shows how an island must looks like.
     *
     * @return A candidate factory instance.
     * @throws Exception
     */
    private static TreeFactory getCandidateFactory(IslandConfiguration configuration, GPConfiguration gpConfiguration)
                               throws Exception
    {
        TreeFactory factory = new TreeFactory(
            gpConfiguration.getWindowSize(),
            configuration.getMaxInitTreeDepth(),
            Probability.EVENS,
            new Probability(configuration.getLeafProbability())
        );

        if (configuration.isEnableBasicOperators()) {
            factory.enableFunctionSet(TreeFactory.BASIC_OPERATORS);
        }

        if (configuration.isEnableComplexOperators()) {
            factory.enableFunctionSet(TreeFactory.COMPLEX_OPERATORS);
        }

        if (configuration.isEnableLogicOperators()) {
            factory.enableFunctionSet(TreeFactory.LOGIC_OPERATORS);
        }

        if (configuration.isEnableTrigonometricOperators()) {
            factory.enableFunctionSet(TreeFactory.TRIGONOMETRIC_OPERATORS);
        }

        factory.enableFunctionSet(TreeFactory.TERMINALS);

        return factory;
    }

}
