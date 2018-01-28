package ks.pdi1;

/**
 * tryb pracy dla tworzenia wzorca hidden
 * ALL - tworzone wszystkie i wybierany najlepszy
 * AVERAGE - reparametryzowany podpis o średnim czasie trwania ze wszystkich
 * MEDIAN - podpis, którego czas jest medianą wszystkich przeznaczonych do stworzenia wzorca
 *
 */
public enum HMode
{
    H_ALL, H_BEST, H_AVERAGE, H_MEDIAN, BEST, ADTW;
}
