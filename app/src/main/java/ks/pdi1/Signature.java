package ks.pdi1;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Signature
{
    /*klasa reprezentująca jeden punkty podpisu (czas, x, y, press)*/
    class Point
    {
        double x;
        double y;
        double press;
        long time;

        Point (double x, double y, double press)
        {
            this.x = x;
            this.y = y;
            this.press = press;
            this.time = System.currentTimeMillis();
        }

        @Override
        public String toString()
        {
            return time + " " + x + " " + y + " " + press;
        }
    }

    String name;
    public List<Point> points;

    Signature()
    {
        this.name = rename();
        this.points = new LinkedList<Point>();
    }

    void addPoint(double x, double y, double press)
    {
        points.add(new Point(x, y, press));
    }

    /*wyświetla w Log listę punktów podpisu*/
    public void print()
    {
        for (Point p : points)
        {
            Log.d("pdi.signature", p.toString());
        }
    }

    /*przekształca surowy zbiór punktów na znormalizowany*/
    public void normalize()
    {
        if (this.points.size()>0)
        {
            this.clearBeginEnd();
            this.resize();
            Log.d("pdi.signature", "signature normalized");
        }
        else
        {
            Log.d("pdi.signature", "can't normalize, !points.size > 0");
        }
    }

    /*zeruje obecny podpis*/
    public void clear()
    {
        name = rename();
        points.clear();
    }

    /*usuwa punkty z zerowym naciskiem na początku i na końcu podpisu
    * podpis musi zawierać jakiekolwiek punkty*/
    private void clearBeginEnd()
    {
        while (points.size()>0 && points.get(0).press == 0.0)
        {
            points.remove(0);
            /*Iterator <Point> iter = points.listIterator();
            if (iter.hasNext()) iter.next();
            iter.remove();*/
        }
        while (points.size()>0 && points.get(points.size()-1).press == 0.0)
        {
            points.remove(points.size()-1);
        }
    }

    /*standaryzuje wartości X i Y od 0 do 1*/
    private void resize()
    {
        //TODO Signature.resize();
    }

    /*ustawia czas względny*/
    private void reTime()
    {
        //TODO Signature.reTime();
        //for (Point)
    }

    private String rename()
    {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd__hh_mm_SS");
        String stringDate = formatter.format(currentDate);
        return stringDate;
    }
}