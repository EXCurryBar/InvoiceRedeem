package dev.edmt.androidcamerarecognitiontext;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Boolean flag = false;
    SurfaceView cameraView;
    public static TextView textView;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Pattern pattern = Pattern.compile("[A-Z]{2}-[0-9]{8}");
    Pattern pattern2 = Pattern.compile("\\D{2}[0-9]{8}");
    Matcher matcher1 ,matcher2;
    public static String[] EightNum = new String[5];
    public static String[] ThreeNum = new String[6];
    public static String debugMessage = "";
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);


        //craw();

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        RequestCameraPermissionID);
                             return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0)
                    {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                String t = "";
                                String tmp = "";
                                for(int i =0;i<items.size();++i)
                                {
                                    TextBlock item = items.valueAt(i);
                                    matcher1 = pattern.matcher(item.getValue().replaceAll(" ",""));
                                    matcher2 = pattern2.matcher(item.getValue().replaceAll(" ",""));
                                    if(matcher2.find()){
                                        t = matcher2.group();
                                        break;
                                    }
                                    else if(matcher1.find()){
                                        t = matcher1.group();
                                    }
                                }
                                for (int i = 2; i < t.length(); i++) {
                                    tmp+=t.charAt(i);
                                }
                                final cr c = new cr();
                                c.start();
                                try{
                                    c.join();
                                }catch (InterruptedException e){
                                    debugMessage+=e.toString()+"\n";
                                    textView.setText(debugMessage);
                                }
                                t = tmp;
                                tmp = c.check(tmp);
                                t = "發票號碼:"+t + "\n"+tmp;
                                textView.setText(t);
                            }
                        });
                    }
                }
            });
        }
    }
}
class cr extends Thread {
    Pattern p1 = Pattern.compile("\\d{8}");
    Pattern p2 = Pattern.compile("\\d{3}");
    String r;
    public cr() {}
    public void run() {
        try {
            URL url = new URL("http://invoice.etax.nat.gov.tw/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                if ((r = reader.readLine()) != null) {
                    Matcher matcher = p1.matcher(r);
                    int g = 0;
                    while (matcher.find() && g < 5) {
                        System.out.println(matcher.group());
                        MainActivity.EightNum[g] = matcher.group();
                        ++g;
                    }
                    matcher = p2.matcher(r);
                    g = 0;
                    while (matcher.find() && g < 19) {
                        if (g > 15) {
                            System.out.println(matcher.group());
                            MainActivity.ThreeNum[g - 16] = matcher.group();
                        }
                        ++g;
                    }
                    g = 2;
                    for (int i = 3; i < MainActivity.ThreeNum.length; i++) {
                        MainActivity.ThreeNum[i] = MainActivity.EightNum[g].substring(5);
                        ++g;
                    }
                    // ==========================================================================
                    // =============================CODE HERE====================================
                }
            } else {
                //System.out.println("伺服器響應代碼為：" + responseCode);
                MainActivity.debugMessage+="伺服器響應代碼為：" + responseCode+"\n";
            }
        } catch (Exception e) {
            System.out.println("獲取不到網頁源碼：" + e);
            MainActivity.debugMessage+=e.toString()+"\n";
        }
    }
    public String check(String invoice) {
        if(invoice.isEmpty())
            return "請對齊發票";
        if (invoice.compareTo(MainActivity.EightNum[0]) == 0)
            return "1000萬";
        if (invoice.compareTo(MainActivity.EightNum[1]) == 0)
            return "200萬";
        for (int i = 2; i < MainActivity.EightNum.length; i++) {
            if (invoice.compareTo(MainActivity.EightNum[i]) == 0)
                return "20萬元";
            if (invoice.substring(1).compareTo(MainActivity.EightNum[i].substring(1)) == 0)
                return "4萬元";
            if (invoice.substring(2).compareTo(MainActivity.EightNum[i].substring(2)) == 0)
                return "1萬元";
            if (invoice.substring(3).compareTo(MainActivity.EightNum[i].substring(3)) == 0)
                return "4千元";
            if (invoice.substring(4).compareTo(MainActivity.EightNum[i].substring(4)) == 0)
                return "1千元";
        }
        for (String s : MainActivity.ThreeNum) {
            if (invoice.substring(5).compareTo(s) == 0) {
                return "2百元";
            }
        }
        return "沒中獎";
    }
}