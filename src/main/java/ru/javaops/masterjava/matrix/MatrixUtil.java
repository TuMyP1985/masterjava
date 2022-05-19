package ru.javaops.masterjava.matrix;

import ru.javaops.masterjava.service.MailService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {




    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final CompletionService<MatrixRezult> completionService = new ExecutorCompletionService<>(executor);

        List<int[]> list = Arrays.asList(matrixA);
        List<Future<MatrixRezult>> futures = list.stream()
                .map(n->completionService.submit(new CalculationJob(matrixA, matrixB, list.indexOf(n))))
                .collect(Collectors.toList());

    /*    List<Future<MatrixRezult>> futures = new ArrayList<>();
        for (int i = 0; i < matrixSize; i++)
            futures.add(completionService.submit(new CalculationJob(matrixA, matrixB, i)));

        return new Callable<int[][]>() {
            @Override
            public int[][] call() {
                while (!futures.isEmpty()) {
                    try {
                        Future<MatrixRezult> future = completionService.poll(10, TimeUnit.SECONDS);
                        if (future == null)
                            break;
                        futures.remove(future);
                        MatrixRezult matrixResult = future.get();
                        matrixC[matrixResult.index] = matrixResult.stringMatrix;
                    } catch (Exception e) { return matrixC; }
                }
                return matrixC;
            }
        }.call();

     */
        try {

            return ((Callable<int[][]>) () -> {
                while (!futures.isEmpty()) {

                    Future<MatrixRezult> future = completionService.poll(10, TimeUnit.SECONDS);
                    if (future == null)
                        break;
                    futures.remove(future);
                    MatrixRezult matrixResult = future.get();
                    matrixC[matrixResult.index] = matrixResult.stringMatrix;

                }
                return matrixC;
            }).call();
        } catch (Exception e) {
            return matrixC;
        }

    }

    public static class CalculationJob implements Callable<MatrixRezult> {
        int[][] matrixA;
        int[][] matrixB;
        int i;

        public CalculationJob(int[][] matrixA, int[][] matrixB, int i) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.i = i;
        }

        @Override
        public MatrixRezult call() throws Exception {
            int matrixSize = matrixA.length;
            int[] stringMatrix = new int[matrixSize];
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                stringMatrix[j] = sum;
            }
            return new MatrixRezult(i,stringMatrix);
        }
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class MatrixRezult{
        public final int index;
        public final int[] stringMatrix;

        public MatrixRezult(int index, int[] stringMatrix) {
            this.index = index;
            this.stringMatrix = stringMatrix;
        }
    }
}
