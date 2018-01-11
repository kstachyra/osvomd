package ks.pdi1;

import android.util.Log;

/**Zmodyfikowana klasa, zmieniona na typ generyczny i podejście do generowania ścieżki marszczenia,
 * na podstawie kodu autorstwa Cheol-Woo Jung (cjung@gatech.edu) i Muhammad Muaaz
 * @param <T> typ przechowywanych i porównywanych danych, musi imlementować Distancable
 */
public class DTW <T extends DTW.Distancable>
{

    protected T[] data1;
    protected T[] data2;
    protected int[][] warpingPath;

    protected int size1;
    protected int size2;
    protected int K;

    protected double warpingDistance;
    protected double accumulatedDist;

    public DTW(T[] sample, T[] template)
    {
        data1 = sample;
        data2 = template;

        size1 = data1.length;
        size2 = data2.length;
        K = 1;

        warpingPath = new int[size1 + size2][2];    // max(size1, size2) <= K < size1 + size2
        warpingDistance = 0.0;
        accumulatedDist = 0.0;

        this.compute();
    }

    /**wyliczanie ścieżki marszczenia i wartości odległości*/
    public void compute()
    {
        double accumulatedDistance = 0.0;

        double[][] d = new double[size1][size2];    // local distances
        double[][] D = new double[size1][size2];    // global distances

        for (int i = 0; i < size1; i++)
        {
            for (int j = 0; j < size2; j++)
            {
                d[i][j] = data1[i].distance(data2[j]);
            }
        }

        D[0][0] = d[0][0];

        for (int i = 1; i < size1; i++)
        {
            D[i][0] = d[i][0] + D[i - 1][0];
        }

        for (int j = 1; j < size2; j++)
        {
            D[0][j] = d[0][j] + D[0][j - 1];
        }

        for (int i = 1; i < size1; i++)
        {
            for (int j = 1; j < size2; j++)
            {
                accumulatedDistance = Math.min(Math.min(D[i - 1][j], D[i - 1][j - 1]), D[i][j - 1]);
                accumulatedDistance += d[i][j];
                D[i][j] = accumulatedDistance;
            }
        }
        accumulatedDistance = D[size1 - 1][size2 - 1];

        int i = size1 - 1;
        int j = size2 - 1;
        int minIndex = 1;

        warpingPath[K - 1][0] = i;
        warpingPath[K - 1][1] = j;

        while ((i + j) != 0)
        {
            if (i == 0)
            {
                j -= 1;
            } else if (j == 0)
            {
                i -= 1;
            } else
            {    // i != 0 && j != 0
                double[] array = {D[i - 1][j], D[i][j - 1], D[i - 1][j - 1]};
                minIndex = this.getIndexOfMinimum(array);

                if (minIndex == 0)
                {
                    i -= 1;
                } else if (minIndex == 1)
                {
                    j -= 1;
                } else if (minIndex == 2)
                {
                    i -= 1;
                    j -= 1;
                }
            } // end else
            K++;
            warpingPath[K - 1][0] = i;
            warpingPath[K - 1][1] = j;
        } // end while
        warpingDistance = accumulatedDistance / K;
        this.accumulatedDist = accumulatedDistance;
        this.reversePath(warpingPath);
    }

    private void reversePath(int[][] path)
    {
        int[][] newPath = new int[K][2];
        for (int i = 0; i < K; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                newPath[i][j] = path[K - i - 1][j];
            }
        }
        warpingPath = newPath;
    }

    private int getIndexOfMinimum(double[] array)
    {
        int index = 0;
        double val = array[0];

        for (int i = 1; i < array.length; i++)
        {
            if (array[i] < val)
            {
                val = array[i];
                index = i;
            }
        }
        return index;
    }

    public String toString()
    {
        String retVal = "Warping Distance: " + warpingDistance + "\n";
        retVal += "Warping Path: {";
        for (int i = 0; i < K; i++)
        {
            retVal += "(" + warpingPath[i][0] + ", " + warpingPath[i][1] + ")";
            retVal += (i == K - 1) ? "}" : ", ";

        }
        return retVal;
    }

    public double getWarpingDistance()
    {
        return warpingDistance;
    }

    public double getAccumulatedDistance()
    {
        return accumulatedDist;
    }

    /**interfejs do zwracania odległości między dwoma obiektami*/
    public interface Distancable<T extends Distancable>
    {
        double distance(T other);
    }

    public static void test()
    {
        Point[] n2 = {new Point(0, 1, 2, 3), new Point(5, 5, 5, 5)};
        Point[] n1 = {new Point(0, 1, 2, 3), new Point(0, 1, 2, 3), new Point(0, 1, 2, 3), new Point(5, 5, 5, 5)};
        DTW<Point> dtw = new DTW<>(n1, n2);



        /*for (int i=0; i<dtw.warpingPath.length; ++i)
        {
            for (int j=0; j<dtw.warpingPath[i].length; ++j)
            {
                Log.d("pdi.DTW", i + " " + j + " ->> " + dtw.warpingPath[i][j]);
            }
        }

        Log.d("pdi.DTW","Accumulated distance between the series is :  " + dtw.getAccumulatedDistance());
        Log.d("pdi.DTW","Warping Distance is between the series is :  " + dtw.getWarpingDistance());*/
        Log.d("pdi.DTW", dtw.toString());
    }
}