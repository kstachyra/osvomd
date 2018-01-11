package ks.pdi1;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public Signature (Signature other)
    {
        this.name = other.name;
        this.ID = other.ID;
        this.points = new LinkedList<Point>();

        for (Point p : other.points)
        {
            this.points.add(new Point(p.time, p.x, p.y, p.press));
        }
    }

    /**z listy podpisów tworzy jeden wzorzec
     * IPPA algorithm
     * @param signatures enrollment signatures
     * @return templateSignature
     */
    static public Signature templateSignature(List<Signature> signatures, int maxInterations)
    {
        Signature template = new Signature();

        //kopia listy signatures
        List<Signature> hiddenSignatures = new LinkedList<Signature>();
        for (Signature s : signatures)
        {
            hiddenSignatures.add(new Signature(s));
        }

        //macierz sprawdzania warunku stopu, przechowuje informacje o poprzednich wynikach marszczenia
        double[][] prevScores = new double[signatures.size()][hiddenSignatures.size()];
        //w celu uniknięcia "oscylacji", sprawdzamy też wyniki dwa kroki przed
        double[][] prevPrevScores = new double[signatures.size()][hiddenSignatures.size()];
        //warunek stopu
        boolean stop = false;
        for (int i=0; i<maxInterations; ++i)
        {
            stop = true;
            LinkedList<Signature> newHidden = new LinkedList<Signature>();
            int hidIdx = 0;
            for (Signature hid : hiddenSignatures)
            {
                int sigIdx = 0;
                LinkedList<Signature> inHiddenTime = new LinkedList<Signature>();
                for (Signature sig : signatures) //dla każdej pary
                {
                    double[] score = new double[1];
                    inHiddenTime.add(sig.warpToTime(hid, score));

                    if (prevScores[sigIdx][hidIdx] != score[0] && prevPrevScores[sigIdx][hidIdx] != score[0]) stop = false; //są różne, więc jeszcze nie koniec

                    prevPrevScores[sigIdx][hidIdx] = prevScores[sigIdx][hidIdx];
                    prevScores[sigIdx][hidIdx] = score[0];
                    ++sigIdx;
                }
                newHidden.add(averageSignature(inHiddenTime));
                ++hidIdx;
            }
            hiddenSignatures = newHidden;

            //nic się już nie zmieniło
            if (stop) break;
        }

        template = pickBestSignature(hiddenSignatures, signatures);
        return template;
    }

    /** z listy proponowanych wzorców, wybiera taki o najmniejszym najgorszym wyniku porównania ze zbiorem enrollemnt
     *
     * @param hiddenSignatures proponowane wzorce
     * @param entrollmentSignatures podpisy na podstawie których wybieramy
     * @return najlepszy podpis
     */
    private static Signature pickBestSignature(List<Signature> hiddenSignatures, List<Signature> entrollmentSignatures)
    {
        double[] worstScores = new double[hiddenSignatures.size()];

        int hidIdx = 0;
        for (Signature h : hiddenSignatures)
        {
            Log.d("pdi.kkk", "indeks " + hidIdx + " wyniki dlan");
            for (Signature e : entrollmentSignatures)
            {

                double newScore = new DTW<Point>(h.getPointArray(), e.getPointArray()).getWarpingDistance();
                Log.d("pdi.kkk", "newScore, czyli wynik porównania " + newScore);

                if (newScore > worstScores[hidIdx]) worstScores[hidIdx] = newScore;
            }
            ++hidIdx;
        }

        int pickIdx = 0;
        double bestScore = Double.MAX_VALUE;
        for (int i=0; i<worstScores.length; ++i)
        {
            if (worstScores[i] < bestScore)
            {
                bestScore = worstScores[i];
                pickIdx = i;
            }
        }
        Log.d("pdi.kkk", "wybrano podpis o inx " + pickIdx + "który miał najgorszą wartość jedynie " + bestScore);
        return hiddenSignatures.get(pickIdx);
    }

    /**z listy podpisów ZMARSZCZONYCH DO TAKIEGO SAMEGO CZASU, wylicza średni podpis
     *
     * @param inHiddenTime lista podpisów zgodnie zmarszczonych
     * @return średni podpis
     */
    @Nullable
    private static Signature averageSignature(final LinkedList<Signature> inHiddenTime)
    {
        //spr czy równe długosci podpisów
        int size = inHiddenTime.getFirst().points.size();
        for (Signature s : inHiddenTime)
        {
            if (s.points.size() != size)
            {
                Log.d("pdi.signature.err", "different signatures size!");
                return null;
            }
        }

        Signature newSig = new Signature();
        //dla każdego punktu
        for (int i=0; i<size; ++i)
        {
            long time = 0;
            double x = 0.0;
            double y = 0.0;
            double press = 0.0;

            //uśrednij ze wszystkich podpisów
            for (Signature s : inHiddenTime)
            {
                time += s.points.get(i).time;
                x += s.points.get(i).x;
                y += s.points.get(i).y;
                press += s.points.get(i).press;
            }
            time = time / inHiddenTime.size();
            x = x / inHiddenTime.size();
            y = y / inHiddenTime.size();
            press = press / inHiddenTime.size();

            //dodaj obliczony punkt
            newSig.addPoint(time, x, y, press);
        }
        return newSig;
    }

    /**przekształca czas podpisu do czasu podanego w parametrze, zgodnie ze ściażką marszczenia
     *
     * @param timeSig podpis, do którego czasu sięodnosimy
     * @param score zmienna, w której zapisywany jest wynik marszczenia
     * @return nowy zmarszczony podpis w czasie podpisu timeSig
     */
    private Signature warpToTime(final Signature timeSig, double[] score)
    {
        Signature newSig = new Signature();

        //pusta lista
        ArrayList<LinkedList<Integer>> map = new ArrayList<>();
        for (int i=0; i<timeSig.points.size(); ++i)
        {
            map.add(new LinkedList<Integer>());
        }

        //oblicz DTW warping path
        DTW<Point> dtw = new DTW<>(this.getPointArray(), timeSig.getPointArray());

        //zapisz wynik ścieżki do zmiennej w paramietrze
        score[0] = dtw.warpingDistance;

        //twórz listę punktów starego podpisu dla punktów czasowych nowego
        for (int[] i : dtw.warpingPath)
        {
            map.get(i[1]).add(i[0]);
        }

        //dla każdego nowego punktu czasowego oblicz (lub skopiuj) odpowiedni nowy (uśredniony) punkt
        for (int i=0; i<timeSig.points.size(); ++i)
        {
            long time = 0;
            double x = 0.0;
            double y = 0.0;
            double press = 0.0;

            //uśrednij wszystkie punkty
            for (int j=0; j<map.get(i).size(); ++j)
            {
                int index = map.get(i).get(j); //indeks marszczonego punktu

                time += this.points.get(index).time;
                x += this.points.get(index).x;
                y += this.points.get(index).y;
                press += this.points.get(index).press;
            }
            time = time / map.get(i).size();
            x = x / map.get(i).size();
            y = y / map.get(i).size();
            press = press / map.get(i).size();

            //dodaj obliczony punkt
            newSig.addPoint(time, x, y, press);
        }
        return newSig;
    }

    /**porównuje dwa podpisy
     *
     * @param sig1 pierwszy podpis
     * @param sig2 drugi podpis
     * @return comaprison value (more -> more different signatures)
     */
    static public double compare(Signature sig1, Signature sig2)
    {
        double value = Double.MAX_VALUE;

        DTW<Point> dtw = new DTW<>(sig1.points.toArray(new Point[0]), sig2.points.toArray(new Point[0]));
        Log.d("pdi.SignatureDTW", dtw.toString());
        Log.d("pdi.SignatureDTW", "warpingDist " + dtw.warpingDistance + " dist " + dtw.accumulatedDist);

        //TODO ustalenie wyniku, wartości jakie wpływają na wynik porównania

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
        String sigString = "";
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
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
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

    /**zwraca tablice Point[] punktów podpisu*/
    public Point[] getPointArray()
    {
        return points.toArray(new Point[0]);
    }


    public void setID(String id)
    {
        this.ID = id;
        rename();
    }

    /**sprawdza, czy podpisy są równe w sensie tożsamości wszystkich punktów*/
    /*@Override
    public boolean equals(Object obj)
    {
        if (obj.getClass() == Signature.class) return false;

        Signature other = (Signature) obj;

        if (this.points.size() != other.points.size()) return false;

        for(int i = 0; i<this.points.size(); ++i)
        {
            if (!this.points.get(i).equals(other.points.get(i)))
            {
                return false;
            }
        }

        return true;
    }*/
}