package ks.pdi1;

import static ks.pdi1.Constants.*;
import static ks.pdi1.Crypto.*;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenHoverListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity
{
    private static final Object lock = true;

    private static String ID = "_";
    private static Signature sig;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int BACKGROUND_LAYER = 42;
    private static final int MAIN_LAYER = 0;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /*this context*/
    private Context mContext;

    /*obsługa Spen*/
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;


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
                sig.addPoint(System.currentTimeMillis(), event.getX(), event.getY(), event.getPressure());
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
                sig.addPoint(System.currentTimeMillis(), event.getX(), event.getY(), event.getPressure());
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

            //Toast.makeText(mContext, "POR button", Toast.LENGTH_LONG).show();
            //captureSpenSurfaceView(sig.name);

            try
            {
                sig.setID(ID);
                writeSigToFile(sig.name + "_RAW", sig.getSigBytes(), false, false);
                sig.normalize();
                writeSigToFile(sig.name, sig.getSigBytes(), false, false);
                writeSigToFile("1", sig.getSigBytes(), true, false);
                captureSpenSurfaceView(sig.name);
                clearCurrentSig();
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            //writeSigToFile("KK");
            //captureSpenSurfaceView("KK");



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
            //Toast.makeText(mContext, "WZ button", Toast.LENGTH_LONG).show();
            //sig.normalize();

            //showDialogWindow();

            try
            {
                //sig2 = readSigFromFile("KK", true, true);
                sig = loadSUSigFile("001_1_1.sig");
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    };

    private final View.OnClickListener button_CLRListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            //Toast.makeText(mContext, "CLR button", Toast.LENGTH_LONG).show();
            clearCurrentSig();

            try
            {
                Signature sig1 = readSigFromFile("1", false, false);
                Signature sig2 = readSigFromFile("2", false, false);
                Signature sig3 = readSigFromFile("3", false, false);


                //sig1.print();

                List<Signature> tempList = new LinkedList<Signature>();
                tempList.add(sig1);
                tempList.add(sig2);
                tempList.add(sig3);

                Signature template = Signature.templateSignature(tempList, MAX_PATTERN_ITERATIONS);

                //template.print();

                //Signature.compare(pattern, sig2);
            } catch (Exception e)
            {
                e.printStackTrace();
            }



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

    /* wyświetla okno dialogowe, i przypisuje wpisaną wartość do zmiennej ID*/
    private void showDialogWindow()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signature ID");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ID = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

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

    /* zapisuje obecny podpis do pliku, z opcją szyfrowania, MODE_PRIVATE*/
    private void writeSigToFile(String filename, byte[] sigBytes, boolean encrypted, boolean modePrivate) throws OSVOMDStorageException, IOException, Exception
    {
        if (encrypted) sigBytes = encrypt(getCryptoKey(), sigBytes);

        FileOutputStream fos = null;
        if (modePrivate)
        {
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
        }
        else
        {
            File mainDir = getFilePath();
            String filePath = mainDir.getAbsolutePath() + "/" + filename + ".txt";
            fos = new FileOutputStream(filePath);
        }
        fos.write(sigBytes);
        fos.close();
    }

    /* czyta podpis z pliku, z możliwością szyfrowania, MODE_PRIVATE*/
    private Signature readSigFromFile(String filename, boolean encrypted, boolean modePrivate) throws OSVOMDStorageException, IOException, Exception
    {
        FileInputStream fis = null;
        byte[] b = null;
        if (modePrivate)
        {
            File file = new File(getFilesDir() + "/" + filename);

            b = new byte[(int) file.length()];

            fis = openFileInput(filename);
        }
        else
        {
            File mainDir = getFilePath();
            String filePath = mainDir.getAbsolutePath() + "/" + filename + ".txt";
            File file = new File(filePath);

            b = new byte[(int) file.length()];

            fis = new FileInputStream(file);
        }

        fis.read(b);

        if (encrypted) b = decrypt(getCryptoKey(), b);

        Signature newSig = new Signature(b);

        if (fis != null) fis.close();

        return newSig;
    }

    /* wczytuje podpis w formacie SUSig*/
    @Nullable
    private Signature loadSUSigFile(String filename) throws IOException, OSVOMDStorageException
    {
        Signature newSig = new Signature();

        FileInputStream is;
        BufferedReader br;
        File mainDir = getFilePath();
        final File file = new File(mainDir.getAbsolutePath() + "/" + filename);

        //wszystkie linie pliku
        LinkedList<String> lines = new LinkedList<String>();

        if (file.exists())
        {
            is = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();

            while(line != null)
            {
                lines.add(line);
                line = br.readLine();
            }
            br.close();
        }
        else
        {
            Log.d("pdi.loadSUSig", "plik " + filename + " " + "nie istnieje");
            return null;
        }

        //usuwa dwie pierwsze linie
        if (lines.size()>2)
        {
            lines.remove(0);
            lines.remove(0);
        }
        else
        {
            Log.d("pdi.loadSUSig", "plik " + filename + " " + "niepoprawny");
            return null;
        }

        for (String line : lines)
        {
            String[] values = line.split(" ");
            if (values.length == 5)
            {
                newSig.addPoint(Long.parseLong(values[2]), Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[3]));
            }
            else
            {
                Log.d("pdi.loadSUSig", "plik " + filename + " " + "niepoprawny");
                return null;
            }
        }
        newSig.setID(filename);
        newSig.normalize();
        newSig.rePress();

        return newSig;
    }

    /* wczytuje podpis w formacie SUSig*/
    private Signature loadSVCFile(String filename)
    {
        //TODO read svc
        return null;
    }

    /*zapisuje obraz podpisu z NoteDocPage jako plik png o zadanej nazwie w folderze domyślnym publicznym*/
    private void captureSpenSurfaceView(String filename)
    {
        File mainDir = null;
        try
        {
            mainDir = getFilePath();
        } catch (OSVOMDStorageException e)
        {
            e.printStackTrace();
        }


        String filePath = mainDir.getAbsolutePath() + "/" + filename + ".png";
        // Save the screen shot as a Bitmap.
        //Bitmap imgBitmap = mSpenSurfaceView.captureCurrentView(true);
        Bitmap imgBitmap = mSpenSurfaceView.capturePage(1);


        OutputStream out = null;
        try
        {
        // Save the Bitmap in the selected location.
            out = new FileOutputStream(filePath);
            imgBitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS, out);

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
    }

    /*zwraca domyślną lokalizację*/
    private File getFilePath() throws OSVOMDStorageException
    {
        // Select the location to save the image.
        File fileCacheItem = Environment.getExternalStoragePublicDirectory(EX_PUB_DIR_PATH);

        //twórz folder, jeśli go nie ma
        if (!fileCacheItem.exists())
        {
            if (!fileCacheItem.mkdirs())
            {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                throw new OSVOMDStorageException();
            }
        }
        return fileCacheItem;
    }

    @Nullable
    private SecretKey getCryptoKey()
    {
        try
        {
            return generateKey(Settings.Secure.ANDROID_ID, APK_CONSTANT);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }
        return null;
    }

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



    /*public boolean drawPoint(View view, float x, float y, float press, int layerId)
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
    };*/

}






