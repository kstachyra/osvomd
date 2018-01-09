package ks.pdi1;

import ks.pdi1.DTW.Distancable;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by KS on 09.01.2018.
 */ /*klasa reprezentująca jeden punkty podpisu (czas, x, y, press)*/
class Point implements Distancable<Point>
{
    double x;
    double y;
    double press;
    long time;

    Point(long time, double x, double y, double press)
    {
        this.x = x;
        this.y = y;
        this.press = press;
        this.time = time;
    }

    @Override
    public String toString()
    {
        return time + "\t" + x + "\t" + y + "\t" + press;
    }

    @Override
    public double distance(Point other)
    {
        return sqrt( pow((this.x - other.x), 2) + pow((this.y - other.y), 2) + pow((this.press - other.press), 2) );
    }
}
