package ks.pdi1;

import android.util.Log;

public class OSVOMDStorageException extends Exception
{
    OSVOMDStorageException()
    {
        super();
        Log.d("pdi.exception", "can't access storage");
    }
}