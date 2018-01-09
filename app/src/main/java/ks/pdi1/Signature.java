package ks.pdi1;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Signature
{
    /* nazwa podpisu, zawierająca ID oraz dokładną datę*/
    String name;
    /* ID podpisu dla rozróżnienia wykonującego*/
    private String ID = "defaultSigId";
    /*lista punktów podpisu (time, x, y, press)*/
    private List<Point> points;

    public Signature()
    {
        this.name = rename();
        this.points = new LinkedList<Point>();
    }

    public Signature (byte[] b)
    {
        this.name = rename();
        this.points = new LinkedList<Point>();

        getDataFromBytes(b);
    }

    /**z listy podpisów tworzy jeden wzorzec
     * IPPA algorithm
     * @param signatures
     * @return hiddenSignature
     */
    static public Signature patternSignature(List<Signature> signatures)
    {
        Signature pattern = new Signature();

        //TODO

        return pattern;
    }

    /**porównuje dwa podpisy
     *
     * @param sig1
     * @param sig2
     * @return comaprison value (more -> more different signatures)
     */
    static public double compare(Signature sig1, Signature sig2)
    {
        double value = Double.MAX_VALUE;
        //TODO
        return value;
    }

    /**porównuje obecny podpis z podanym w parametrze
     *
     * @param other signature
     * @return omaprison value (more -> more different signatures)
     */
    public double compareTo(Signature other)
    {
        return Signature.compare(this, other);
    }

    /**dodaje punkt do podpisu*/
    public void addPoint(long time, double x, double y, double press)
    {
        points.add(new Point(time, x, y, press));
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

    /*zwraca podpis jako pojedynczy string*/
    private String getSigString()
    {
        String sigString = new String();
        for (Point p : points)
        {
            sigString = sigString.concat(p.toString()).concat(System.lineSeparator());
        }
        return sigString;
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

    /*zmienia nazwę podpisu na zgodną z aktualnym ID i datą*/
    private String rename()
    {
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
        String stringDate = formatter.format(currentDate);
        stringDate = ID + stringDate;
        name = stringDate;
        return stringDate;
    }

    /*zeruje obecny podpis*/
    public void clear()
    {
        try
        {
            rename();
            points.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*zwraca podpis jako ciąg bajtów*/
    public byte[] getSigBytes()
    {
        return this.getSigString().getBytes(StandardCharsets.UTF_8);
    }

    /*ustawia punkty podpisu na podstawie danych z tablicy bajtów (zgodnie z getSigBytes*/
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

    /*wyświetla w Log listę punktów podpisu*/
    public void print()
    {
        for (Point p : points)
        {
            Log.d("pdi.signature", p.toString());
        }
    }


    public void setID(String id)
    {
        this.ID = id;
        rename();
    }
}