package ai.hual.labrador.nlu.utils;

import ai.hual.labrador.exceptions.NLUException;
import io.jenetics.DoubleGene;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;

import java.util.Arrays;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

/**
 * Using Evolutionary method to train the weight of 4 answer template
 * and try to get the optimized parameters to reach maximum correct answer
 * <p>
 * Usage e.g:
 * <pre>
 *      result = EvolutionaryParameters.computeParam(double[][]scores, int[][]marks);
 *  </pre>
 */
public class EvolutionaryParameter {
    private static int paramsNum;
    private static double scores[][];
    private static double alphaScores[][];
    private static int marks[][];
    private static double[] result;
    private static double percentage;
    private static boolean withRefused;

    /**
     * Get the final parameters
     *
     * @param scores original score
     * @param marks  correctness ground truth
     * @return OptimizedParameters;
     */
    public static OptimizedParameters computeParam(double[][] scores, int[][] marks) {
        assert scores.length == marks.length;
        if (scores.length == 0)
            throw new NLUException("Error, data file empty");
        assert scores[0].length == marks[0].length;
        paramsNum = scores[0].length;
        EvolutionaryParameter ep = new EvolutionaryParameter();
        ep.setParams(scores, marks);
        ep.train();
        OptimizedParameters res = new OptimizedParameters();
        res.setAlpha(Arrays.copyOfRange(result, 0, paramsNum));
        res.setRefuseScore(Arrays.copyOfRange(result, paramsNum, paramsNum * 2));
        res.setPercentage(percentage);
        return res;
    }

    /**
     * Get the final parameters
     *
     * @param scores      original score
     * @param marks       correctness ground truth
     * @param withRefused allow refused
     * @return OptimizedParameters;
     */
    public static OptimizedParameters computeParam(double[][] scores, int[][] marks, boolean withRefused) {
        assert scores.length == marks.length;
        if (scores.length == 0)
            throw new NLUException("Error, data file empty");
        assert scores[0].length == marks[0].length;
        paramsNum = scores[0].length;
        EvolutionaryParameter ep = new EvolutionaryParameter();
        ep.setParams(scores, marks, withRefused);
        ep.train();
        OptimizedParameters res = new OptimizedParameters();
        res.setAlpha(Arrays.copyOfRange(result, 0, paramsNum));
        res.setRefuseScore(Arrays.copyOfRange(result, paramsNum, paramsNum * 2));
        res.setPercentage(percentage);
        return res;
    }

    /**
     * fitness function for evolutionary
     *
     * @param alpha The parameters we want to train;
     *              First paramsNum parameters are the exponent of each template;
     *              Second paramsNum parameters are refusing score of each template;
     * @return Total correct number.
     */
    private static int fitness(final double[] alpha) {
        int totalCorrect = 0;
        for (int i = 0; i < marks.length; i++) {
            int chooseNum = -2;
            for (int j = 0; j < paramsNum; j++) {
                alphaScores[i][j] = Math.pow(scores[i][j], alpha[j]);
                if (alphaScores[i][j] - alpha[j + paramsNum] >= 0) continue;
                else alphaScores[i][j] = 0;
            }
            for (int j = 0; j < paramsNum; j++) {
                double max = 0;
                for (int k = 0; k < paramsNum; k++) {
                    if (alphaScores[i][k] > max) {
                        max = alphaScores[i][k];
                    }
                }
                if (max == 0) {
                    if (withRefused)
                        chooseNum = -1;
                    break;
                } else if (alphaScores[i][j] == max) {
                    chooseNum = j;
                    break;
                }
            }
            if (chooseNum != -2) {
                if (chooseNum == -1 && withRefused) {
                    int isZero = 0;
                    for (int j = 0; j < paramsNum; j++) {
                        isZero += marks[i][j];
                    }
                    if (isZero == 0)
                        totalCorrect++;
                } else {
                    if (marks[i][chooseNum] == 1)
                        totalCorrect++;
                }
            }
        }
        return totalCorrect;
    }

    /**
     * Using Jenetics lib to implement evolutionary algorithm;
     */
    private static void train() {
        final Engine<DoubleGene, Integer> engine = Engine
                .builder(
                        EvolutionaryParameter::fitness,
                        Codecs.ofVector(DoubleRange.of(0, 1), paramsNum * 2))
                .populationSize(500)
                .maximizing()
                .alterers(
                        new Mutator<>(0.075),
                        new MeanAlterer<>(0.5))
                .build();
        final EvolutionStatistics<Integer, ?>
                statics = EvolutionStatistics.ofNumber();
        final Phenotype<DoubleGene, Integer> best = engine.stream()
                .limit(bySteadyFitness(15))
                .limit(100)
                .peek(statics)
                .collect(toBestPhenotype());

        result = new double[paramsNum * 2];
        for (int i = 0; i < paramsNum * 2; i++) {
            int length = best.getGenotype().getChromosome().getGene(i).toString().length();
            result[i] = Double.parseDouble(
                    best.getGenotype()
                            .getChromosome()
                            .getGene(i)
                            .toString()
                            .substring(1, length - 1));
        }
        int correctAnswerNum = best.getFitness().intValue();
        percentage = (double) correctAnswerNum / marks.length;
    }


    private static void setParams(double[][] scores, int[][] marks) {
        EvolutionaryParameter.scores = scores;
        EvolutionaryParameter.marks = marks;
        EvolutionaryParameter.alphaScores = new double[scores.length][paramsNum];
    }

    private static void setParams(double[][] scores, int[][] marks, boolean withRefused) {
        EvolutionaryParameter.scores = scores;
        EvolutionaryParameter.marks = marks;
        EvolutionaryParameter.alphaScores = new double[scores.length][paramsNum];
        EvolutionaryParameter.withRefused = withRefused;
    }

}

