package ks.pdi1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.*;
import com.samsung.android.sdk.pen.document.*;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenHoverListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final Object lock = true;

    private static Signature sig;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int BACKGROUND_LAYER = 42;
    private static final int MAIN_LAYER = 0;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /*część okna przeznaczona na podpis*/
    private static final double SIZE = 0.8f;

    /*this context*/
    private Context mContext;

    /*obsługa Spen*/
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    //private int mToolType = SpenSurfaceView.TOOL_SPEN;


    //LISTENERY
    private final SpenTouchListener mPenTouchListener = new SpenTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            if (event.getToolType(0) == SpenSurfaceView.TOOL_SPEN)
            {
                mSpenPageDoc.setCurrentLayer(MAIN_LAYER);
                Log.d("pdi.sigdata", event.getX() + " " + event.getY() + " " + event.getPressure());
                sig.addPoint(event.getX(), event.getY(), event.getPressure(), System.currentTimeMillis());
                return true;
            }
            return false;
        }
    };

    private final SpenHoverListener onMyHoverEvent = new SpenHoverListener()
    {
        @Override
        public boolean onHover(View view, MotionEvent event)
        {
            if (event.getToolType(0) == SpenSurfaceView.TOOL_SPEN)
            {
                mSpenPageDoc.setCurrentLayer(MAIN_LAYER);
                Log.d("pdi.sigdata", event.getX() + " " + event.getY() + " " + event.getPressure());
                sig.addPoint(event.getX(), event.getY(), event.getPressure(), System.currentTimeMillis());
                return true;
            }
            return false;
        }
    };

    private final View.OnClickListener button_PORListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            Toast.makeText(mContext, "POR button", Toast.LENGTH_LONG).show();
            sig.print();

            captureSpenSurfaceView();
            saveSigToFile();



            /*czyszczenie strony
            mSpenPageDoc.removeAllObject();
            mSpenSurfaceView.update();*/

            /*zapisywanie scrennshota
            captureSpenSurfaceView();*/

            /*wyswietlanie obrazka
            kkk(mSpenSurfaceView, 150, 150);*/

            /*wątek rysujący
            thread.start();*/

        }
    };



    private final View.OnClickListener button_WZListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Toast.makeText(mContext, "WZ button", Toast.LENGTH_LONG).show();
            sig.normalize();
        }
    };

    private final View.OnClickListener button_CLRListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Toast.makeText(mContext, "CLR button", Toast.LENGTH_LONG).show();
            clearCurrentSig();

            /*synchronized (lock)
            {
                mSpenPageDoc.setCurrentLayer(BACKGROUND_LAYER);
                Toast.makeText(mContext, "CLR bckgl" + mSpenPageDoc.getObjectCount(true), Toast.LENGTH_LONG).show();


                ArrayList <SpenObjectBase> imgList = mSpenPageDoc.getObjectList(SpenPageDoc.FIND_TYPE_IMAGE);

                for (SpenObjectBase img : imgList)
                {
                    mSpenPageDoc.removeObject(img);
                }

                mSpenSurfaceView.update();
                mSpenPageDoc.setCurrentLayer(MAIN_LAYER);
            }*/
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        verifyStoragePermissions(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        sig = new Signature();

        initSpen();
        addListeners();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //thread.interrupt();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //finish();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        clearSpen();
        //thread.interrupt();
    }

    /*inicjalizacja SPen, tworzenie SpenNoteDoc, mSpenPageDoc, mSpenSurfaceView*/
    private void initSpen()
    {
        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        //inicjalizacja Spen SDK
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try
        {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e)
        {
            Toast.makeText(mContext, "This device does not support Spen.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e1)
        {
            Toast.makeText(mContext, "Cannot initialize Pen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        //towrzenie SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);



        if (mSpenSurfaceView == null)
        {
            Toast.makeText(mContext, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);

        //wymiary erkanu
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);

        //tworzenie SpenNoteDoc.
        try
        {
            mSpenNoteDoc = new SpenNoteDoc(mContext, (int) (rect.width() * SIZE), (int) (rect.height() * SIZE));
        } catch (IOException e)
        {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e)
        {
            e.printStackTrace();
            finish();
        }

        //wyłączanie gestów palcami
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_NONE);

        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_SPEN, SpenSurfaceView.ACTION_STROKE);


        //nowa strona w SpenDoc
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        //mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.setBackgroundColor(0xFFFFFFFF);

        //nowa warstwa obiektów tła
        mSpenPageDoc.appendLayer(BACKGROUND_LAYER);


        //dodawanie do widoku strony
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
        if (isSpenFeatureEnabled == false)
        {
            //mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /*czyszczenie pamięci - SpenNoteDoc, mSpenPageDoc, mSpenSurfaceView*/
    private void clearSpen()
    {
        if (mSpenSurfaceView != null)
        {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }
        if (mSpenNoteDoc != null)
        {
            try
            {
                mSpenNoteDoc.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    }

    /*dodawanie listenerów touch/hover oraz przyciski*/
    private void addListeners()
    {
        mSpenSurfaceView.setHoverListener(onMyHoverEvent);
        mSpenSurfaceView.setTouchListener(mPenTouchListener);

        Button button_POR = (Button) findViewById(R.id.button_POR);
        button_POR.setOnClickListener(button_PORListener);

        Button button_WZ = (Button) findViewById(R.id.button_WZ);
        button_WZ.setOnClickListener(button_WZListener);

        Button button_CLR = (Button) findViewById(R.id.button_CLR);
        button_CLR.setOnClickListener(button_CLRListener);
    }

    /*czyści obecnie tworzony podpis*/
    private void clearCurrentSig()
    {
        //czyszczenie obecnego podpisu
        sig.clear();

        //czyszczenie stroke'ow z SpenPageDoc
        synchronized (lock)
        {
            mSpenPageDoc.setCurrentLayer(MAIN_LAYER);
            ArrayList<SpenObjectBase> strkList = mSpenPageDoc.getObjectList(SpenPageDoc.FIND_TYPE_STROKE);
            for(SpenObjectBase stroke : strkList)
            {
                mSpenPageDoc.removeObject(stroke);
            }
            mSpenSurfaceView.update();
        }
    }

    //TODO na razie publicznie wszystko
    /*zapisuje plik z podpisem*/
    private void saveSigToFile()
    {
        //lokalizacja
        File fileCacheItem = Environment.getExternalStoragePublicDirectory(Constants.EX_PUB_DIR_PATH);

        //twórz folder, jeśli go nie ma
        if (!fileCacheItem.exists())
        {
            if (!fileCacheItem.mkdirs())
            {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String filePath = fileCacheItem.getAbsolutePath() + "/" + sig.name + ".txt";

        OutputStream out = null;
        try
        {
            out = new FileOutputStream(filePath);
            out.write(sig.getSigBytes());

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void captureSpenSurfaceView()
    {

        // Select the location to save the image.
        File fileCacheItem = Environment.getExternalStoragePublicDirectory(Constants.EX_PUB_DIR_PATH);

        //twórz folder, jeśli go nie ma
        if (!fileCacheItem.exists())
        {
            if (!fileCacheItem.mkdirs())
            {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String filePath = fileCacheItem.getAbsolutePath() + "/" + sig.name + ".png";
        // Save the screen shot as a Bitmap.
        //Bitmap imgBitmap = mSpenSurfaceView.captureCurrentView(true);
        Bitmap imgBitmap = mSpenSurfaceView.capturePage(1);


        OutputStream out = null;
        try
        {
        // Save the Bitmap in the selected location.
            out = new FileOutputStream(filePath);
            imgBitmap.compress(Bitmap.CompressFormat.PNG, Constants.COMPRESS, out);

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        imgBitmap.recycle();
        Toast.makeText(mContext, filePath, Toast.LENGTH_LONG).show();
    }

    public int aa = 150;
    public int bb = 150;

    Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                while(true) {




                    sleep(200);
                    drawPoint(mSpenSurfaceView, aa, bb, 0.5f, BACKGROUND_LAYER);

                    aa += 2;
                    bb+= 1;
                }
            } catch (InterruptedException e) {
                Log.d("pdi.thread", "przerwano wątek", e);
            }
        }
    };





    public static void verifyStoragePermissions(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(

                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



    public boolean drawPoint(View view, float x, float y, float press, int layerId)
    {
            SpenControlBase control = mSpenSurfaceView.getControl();
            if (control == null)
            {
// Set Bitmap file for ObjectImage.
                    SpenObjectImage imgObj = new SpenObjectImage();
                    Bitmap imageBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_name);
                    imgObj.setImage(imageBitmap);
// Set the position of ObjectImage and add it to PageDoc.



                    float panX = mSpenSurfaceView.getPan().x;
                    float panY = mSpenSurfaceView.getPan().y;
                    float zoom = mSpenSurfaceView.getZoomRatio();
                    float imgWidth = imageBitmap.getWidth() * zoom;
                    float imgHeight = imageBitmap.getHeight() * zoom;
                    RectF imageRect = new RectF();
                    imageRect.set((x - imgWidth / 2) / zoom + panX,
                            (y - imgHeight / 2) / zoom + panY,
                            (x + imgWidth / 2) / zoom + panX,
                            (y + imgHeight / 2) / zoom + panY);
                    imgObj.setRect(imageRect, true);


                synchronized (lock)
                {
                    mSpenPageDoc.setCurrentLayer(layerId);
                    mSpenPageDoc.appendObject(imgObj);
                    mSpenSurfaceView.update();
                    mSpenPageDoc.setCurrentLayer(MAIN_LAYER);
                }

                    imageBitmap.recycle();



                return true;
            }
        return true;
    }






}






