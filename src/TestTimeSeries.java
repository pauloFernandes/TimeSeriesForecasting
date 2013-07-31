import geneticProgramming.functions.Node;
import geneticProgramming.functions.function.Simplification;
import geneticProgramming.geneticOperators.*;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.Stagnation;
import org.uncommons.watchmaker.framework.termination.TargetFitness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: paulo
 * Date: 29/07/13
 * Time: 21:13
 */
public class TestTimeSeries
{

    /**
     * Constantes que sao candidatos a parametros.
     *
     * Incluir como candidatos a parametros:
     *   Grupo de operadores a ser incluidos para a classe TreeFactory.
     *   Grupo de operadores geneticos que serão incluidos na lista de operadores geneticos.
     *   Grupo de condições de parada.
     *
     */
    private static final int TIME_SERIES_SIZE        = 200;
    private static final int POPULATION_SIZE         = 100;
    private static final int ELITISM_COUNT           = 5;
    private static final int WINDOW_SIZE             = 2;
    private static final int MAXIMUM_TREE_DEPTH      = 4;
    private static final double LEAF_NODE_PROBABILITY = 0.6d;
    private static final double MUTATION_PROBABILITY  = 0.5d;
    private static final int MAX_DEPTH                = 10;

    private static double SURVIVAL_PROBABILITY       = 0.01d;
    private static int AMOUNT_OF_PLAGUE_SPREADS      = 3;
    private static int GENERATIONS_BEFORE_PLAGUE     = 1000;
    private static final boolean IS_FITNESS_NATURAL = false;
    private static final int TARGET_FITNESS         = 0;
    private static final int MAX_GENERATION_COUNT   = 10000;
    private static final int STAGNATION_LIMIT       = 5000;
    private static final boolean VERBOSE_EVOLVE     = true;
    private static final int PRINT_LOG_INTERVAL     = 100;
    private static final int FITNESS_TYPE           = TimeSeriesEvaluator.PLAIN_ERROR;


    public static void main(String[] args)
    {
        double[] data = TestTimeSeries.getData();
        Node program  = TestTimeSeries.evolveProgram(data);
        System.out.println("Best solution found");
        System.out.println(program.print());
    }

    private static double[] getData()
    {
        double[] series  = new double[TestTimeSeries.TIME_SERIES_SIZE];
        double initial   = new Random(100).nextDouble();
        double increase  = new Random(10).nextDouble();
        series[0]        = initial;
        for (int i = 1; i < TestTimeSeries.TIME_SERIES_SIZE; i++) {
            series[i] = series[i-1] + increase;
        }

        return series;
    }

    private static Node evolveProgram(double[] data)
    {
        Node node = null;
        TerminationCondition[] terminationConditions = getTerminationConditions();
        EvolutionEngine<Node> engine                 = getEvolutionEngine(data);
        try {
            addEvolutionObservers(engine);
            node = engine.evolve(POPULATION_SIZE, ELITISM_COUNT, terminationConditions);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Termination conditions satisfied: ");
            List<TerminationCondition> a = engine.getSatisfiedTerminationConditions();
            for (TerminationCondition condition : a) {
                System.out.println(condition.toString());
            }
        }

        return node;
    }

    private static EvolutionEngine<Node> getEvolutionEngine(double[] data)
    {
        TreeFactory                factory               = TestTimeSeries.getTreeFactory();
        EvolutionaryOperator<Node> evolutionaryOperators = TestTimeSeries.getEvolutionaryOperators(factory);
        FitnessEvaluator<Node>     fitnessEvaluator      = TestTimeSeries.getFitnessEvaluator(data);
        SelectionStrategy          selectionStrategy     = TestTimeSeries.getSelectionStrategy();
        return new GenerationalEvolutionEngine<Node>(
                factory, evolutionaryOperators, fitnessEvaluator, selectionStrategy, new MersenneTwisterRNG()
        );
    }

    private static TreeFactory getTreeFactory()
    {
        TreeFactory factory = new TreeFactory (
            TestTimeSeries.WINDOW_SIZE,
            TestTimeSeries.MAXIMUM_TREE_DEPTH,
            Probability.EVENS,
            new Probability(TestTimeSeries.LEAF_NODE_PROBABILITY)
        );
        try {
            factory.enableFunctionSet(TreeFactory.BASIC_OPERATORS);
            factory.enableFunctionSet(TreeFactory.COMPLEX_OPERATORS);
            factory.enableFunctionSet(TreeFactory.LOGIC_OPERATORS);
            factory.enableFunctionSet(TreeFactory.TRIGONOMETRIC_OPERATORS);
            factory.enableFunctionSet(TreeFactory.TERMINALS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return factory;
    }

    private static EvolutionaryOperator<Node> getEvolutionaryOperators(TreeFactory treeFactory)
    {
        List<EvolutionaryOperator<Node>> operators = new ArrayList<EvolutionaryOperator<Node>>();
        operators.add(new TreeMutation(treeFactory, new Probability(TestTimeSeries.MUTATION_PROBABILITY)));
        operators.add(new TreeCrossover(TestTimeSeries.MAX_DEPTH));
        operators.add(new Simplification());
        operators.add(new Plague(
            treeFactory, TestTimeSeries.SURVIVAL_PROBABILITY, TestTimeSeries.AMOUNT_OF_PLAGUE_SPREADS,
            TestTimeSeries.GENERATIONS_BEFORE_PLAGUE, TestTimeSeries.IS_FITNESS_NATURAL
        ));

        return new EvolutionPipeline<Node>(operators);
    }

    private static FitnessEvaluator<Node> getFitnessEvaluator(double[] data)
    {
        return new TimeSeriesEvaluator(data, TestTimeSeries.WINDOW_SIZE, TestTimeSeries.FITNESS_TYPE);
    }

    private static TerminationCondition[] getTerminationConditions()
    {
        // Preparar uma forma de parametrizacao para a quantidade de condicoes de parada.
        TerminationCondition[] terminationConditions = new TerminationCondition[3];
        terminationConditions[0] = new TargetFitness(TestTimeSeries.TARGET_FITNESS, TestTimeSeries.IS_FITNESS_NATURAL);
        terminationConditions[1] = new GenerationCount(TestTimeSeries.MAX_GENERATION_COUNT);
        terminationConditions[2] = new Stagnation(TestTimeSeries.STAGNATION_LIMIT, TestTimeSeries.IS_FITNESS_NATURAL);

        return terminationConditions;
    }

    private static SelectionStrategy getSelectionStrategy()
    {
        return new RouletteWheelSelection();
    }

    private static void addEvolutionObservers(EvolutionEngine<Node> engine) {
        engine.addEvolutionObserver(new EvolutionObserver<Node>() {
            @Override
            public void populationUpdate(PopulationData<? extends Node> populationData) {
                if (TestTimeSeries.VERBOSE_EVOLVE) {
                    if (populationData.getGenerationNumber() % TestTimeSeries.PRINT_LOG_INTERVAL == 0) {
                        System.out.println("Generation: " + populationData.getGenerationNumber());
                        System.out.println("\tBest Solution: " + populationData.getBestCandidate());
                        System.out.println("\tIts Fitness is: " + populationData.getBestCandidateFitness());
                        System.out.println("\tPopulation size: " + populationData.getPopulationSize());
                        System.out.println("-----------------------------------------------------------");
                    }
                }

                if (populationData.getBestCandidateFitness() == TestTimeSeries.TARGET_FITNESS) {
                    System.out.println("=============================================================");
                    System.out.println("======================== FINAL RESULT =======================");
                    System.out.println("=============================================================");
                    System.out.println("Generation: " + populationData.getGenerationNumber());
                    System.out.println("\tBest Solution: " + populationData.getBestCandidate());
                    System.out.println("\tIts Fitness is: " + populationData.getBestCandidateFitness());
                    System.out.println("\tPopulation size: " + populationData.getPopulationSize());
                    System.out.println("-----------------------------------------------------------");
                }
            }
        });
    }

}
