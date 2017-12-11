package ks.pdi1;

import android.os.Environment;

import java.nio.charset.StandardCharsets;

public class Constants
{
    public final static String EX_PUB_DIR_PATH = "/OSVOMD";

    /*stopień kompresji obrazów PNG podpisu 0 (MAX compression) - 100 (LEAST cmpression)*/
    public final static int COMPRESS = 50;


    /*Crypto*/
    public final static int ITERATIONS = 1000;
    public final static int KEY_LENGTH = 256;

    public final static String APK_CONSTANT = "apk_constant";
}