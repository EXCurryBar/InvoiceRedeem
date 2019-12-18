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
import android.widget.Toast;

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
    SurfaceView cameraView;
    public static TextView textView;
    public static TextView textView2;
    public static TextView textView3;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Pattern pattern = Pattern.compile("[A-Z]{2}-[0-9]{8}");
    Pattern pattern2 = Pattern.compile("\\D{2}[0-9]{8}");
    Pattern pattern3 = Pattern.compile("\\d{2}-\\d{2}");
    Matcher matcher1, matcher2, matcher3;
    public static String[] EightNum = new String[5];
    public static String[] ThreeNum = new String[6];
    public static String debugMessage = "";
    public static String date = "";
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
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);


        final cr c = new cr();
        c.start();
        try{
            c.join();
        }catch (InterruptedException e){
            debugMessage+=e.toString()+"\n";
            textView.setText(debugMessage);
        }
        textView3.setText(date.isEmpty()?"":date);



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
                            Boolean flag = false;
                            Boolean dateFound = false;
                            @Override
                            public void run() {
                                String t = "";
                                String tmp = "";
                                if(debugMessage.isEmpty()){
                                    for(int i =0;i<items.size();++i)
                                    {
                                        TextBlock item = items.valueAt(i);
                                        matcher1 = pattern.matcher(item.getValue());
                                        matcher2 = pattern2.matcher(item.getValue());
                                        matcher3 = pattern3.matcher(item.getValue());
                                        if(matcher3.find()){
                                            String in = matcher3.group();
                                            flag = date.substring(4,9).equals(in);
                                            dateFound = !in.isEmpty();
                                        }
                                        if(matcher2.find()){
                                            t = matcher2.group();
                                            break;
                                        }
                                        else if(matcher1.find()){
                                            t = matcher1.group();
                                            break;
                                        }
                                        textView.setText("發票號碼 : ");
                                        textView2.setText("");
                                    }
                                    for (int i = 2; i < t.length(); i++)
                                        tmp+=t.charAt(i);
                                    t = tmp;
                                    tmp = c.check(tmp);
                                    if(dateFound){
                                        if(flag){
                                            textView2.setTextSize(36);
                                            t = "發票號碼 : "+t +(tmp.compareTo("請對齊發票")==0?tmp:"");
                                            textView.setText(t);
                                            textView2.setText((tmp.compareTo("請對齊發票")==0?"":tmp));
                                        }else if((tmp.compareTo("請對齊發票")!=0)){
                                            String warningMessage = "這不是本月份發票哦!";
                                            textView2.setTextSize(22);
                                            textView2.setText(warningMessage);
                                        }
                                    }else {
                                        t = "發票號碼 : 請對齊發票";
                                        textView.setText(t);
                                    }
                                }else{
                                    textView.setText("請檢查網路連線並重啟程式");
                                }
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
    Pattern p2 = Pattern.compile("<span id=\"newAddSixPrize\" class=\"t18Red\">.*</span>");
    Pattern p3 = Pattern.compile("\\d{3}年\\d{2}-\\d{2}月");
    String r, tmp;
    int check;
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
                    if(matcher.find())
                        tmp = matcher.group();
                    tmp = tmp.substring(41,52).replaceAll(" ", "");
                    tmp = tmp.replaceAll("、", "");
                    for (int i = 0; i < tmp.length()/3; i++){
                        MainActivity.ThreeNum[i] = tmp.substring(i*3,i*3+3);
                        check = i;
                    }
                    g = 2;
                    for (int i = check+1; i < MainActivity.ThreeNum.length; i++) {
                        MainActivity.ThreeNum[i] = MainActivity.EightNum[g].substring(5);
                        ++g;
                    }
                    matcher = p3.matcher(r);
                    if(matcher.find())
                        MainActivity.date = matcher.group();
                }
            } else {
                MainActivity.debugMessage+="伺服器響應代碼為：" + responseCode+"\n";
            }
        } catch (Exception e) {
            //System.out.println("獲取不到網頁源碼：" + e);
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