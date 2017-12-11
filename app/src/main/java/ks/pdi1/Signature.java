package ks.pdi1;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static ks.pdi1.Crypto.*;

public class Signature
{
    /*klasa reprezentująca jeden punkty podpisu (czas, x, y, press)*/
    class Point
    {
        double x;
        double y;
        double press;
        long time;

        Point (long time, double x, double y, double press)
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
    }

    String name;
    public List<Point> points;

    Signature()
    {
        this.name = rename();
        this.points = new LinkedList<Point>();
    }

    Signature (byte[] b)
    {
        this.name = rename();
        this.points = new LinkedList<Point>();

        getDataFromBytes(b);
    }

    void addPoint(long time, double x, double y, double press)
    {
        points.add(new Point(time, x, y, press));
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
        try
        {
            if (this.points.size() > 0)
            {
                this.clearBeginEnd();
                this.resize();
                this.reTime();
                Log.d("pdi.signature", "signature normalized");
            } else
            {
                Log.d("pdi.signature", "can't normalize, !points.size > 0");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*zeruje obecny podpis*/
    public void clear()
    {
        try
        {
            name = rename();
            points.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*zwraca podpis jako pojedynczy string*/
    public String getSigString()
    {
        String sigString = new String();
        for (Point p : points)
        {
            sigString = sigString.concat(p.toString()).concat(System.lineSeparator());
        }
        return sigString;
    }

    /*zwraca podpis jako ciąg bajtów*/
    public byte[] getSigBytes()
    {
        return this.getSigString().getBytes(StandardCharsets.UTF_8);
    }

    /*usuwa punkty z zerowym naciskiem na początku i na końcu podpisu*/
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
        //znajdź min i max wartości punktów
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;
        for (Point p : points)
        {
            if(p.x < minX) minX = p.x;
            if(p.x > maxX) maxX = p.x;
            if(p.y < minY) minY = p.y;
            if(p.y > maxY) maxY = p.y;
        }

        //znormalizuj od 0 do 1
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        for (Point p : points)
        {
            p.x = (p.x - minX)/rangeX;
            p.y = (p.y - minY)/rangeY;
        }
    }

    /*ustawia czas względny, względem pierwszego punktu*/
    private void reTime()
    {
        long firstPointTime = points.get(0).time;
        for (Point p : points)
        {
            p.time -= firstPointTime;
        }
    }

    private String rename()
    {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
        String stringDate = formatter.format(currentDate);
        return stringDate;
    }

    private void getDataFromBytes(byte[] b)
    {
        String sigString = new String(b, StandardCharsets.UTF_8);
        String[] lines = sigString.split(System.lineSeparator());
        for (String line : lines)
        {
            String[] values = line.split("\t");
            if (values.length == 4)
            {
                this.addPoint(Long.parseLong(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
            }
        }
    }

}