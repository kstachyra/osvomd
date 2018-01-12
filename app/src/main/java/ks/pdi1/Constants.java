package ks.pdi1;

public class Constants
{
    /*część okna przeznaczona na podpis*/
    public static final double SIZE = 0.8f;

    /*nazwa publicznego folderu aplikacji*/
    public final static String EX_PUB_DIR_PATH = "/OSVOMD";

    /*stopień kompresji obrazów PNG podpisu 0 (MAX compression) - 100 (MIN compression)*/
    public final static int COMPRESS = 0;

    /*Crypto*/
    public final static int CRYPTO_ITERATIONS = 1000;
    public final static int CRYPTO_KEY_LENGTH = 256;

    public final static String APK_CONSTANT = "asdkl;";

    /*pattern*/
    public final static int MAX_PATTERN_ITERATIONS = 10;

    /*compare*/
    public final static double DTW_TIME_WEIGHT = 0.000001;

    /*procentowa ignorowana róznica czasu*/
    public final static double SIGNATURE_TIME_LIMIT = 0.10;
    public final static double SIGNATURE_TIME_WEIGHT = 1;
}