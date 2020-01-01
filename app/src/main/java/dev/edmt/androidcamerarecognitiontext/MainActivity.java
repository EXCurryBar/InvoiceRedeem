package dev.edmt.androidcamerarecognitiontext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    SurfaceView cameraView;
    public static TextView textView;
    public static TextView textView2;
    public static TextView textView3;
    public static Spinner spinner;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Pattern pattern2 = Pattern.compile("\\D{2}[-| ][0-9]{8}");
    Pattern pattern3 = Pattern.compile("\\D{2}[0-9]{8}");
    Pattern pattern4 = Pattern.compile("\\d-\\d{2}[^-| ]");
    Matcher matcher2, matcher3, matcher4;
    public static String[] EightNum = new String[5];
    public static String[] ThreeNum = {"","","","","",""};
    public static String debugMessage = "";
    public static String date = "",gottenMessage = "";
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCameraPermissionID) {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        spinner = (Spinner)findViewById(R.id.spinner);
        Calendar calendar = Calendar.getInstance();
        final int month = calendar.get(Calendar.MONTH)+1;
        final int p =0;

        final cr c = new cr(month,p);
        c.start();
        try{
            c.join();
        }catch (InterruptedException e){
            debugMessage+=e.toString()+"\n";
            textView.setText(debugMessage);
        }
        //textView3.setText(date.isEmpty()?"":date.substring(0,6)+"-"+date.substring(6));
        String[] spList = {c.process(month,0),c.process(month,1)};
        final ArrayAdapter<String> adapter =new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 cr ss = new cr(month,position);
                 ss.start();
                try{
                   ss.join();
                    textView3.setText(date.isEmpty()?"":date.substring(0,6)+"-"+date.substring(6));
                    Toast.makeText(MainActivity.this, "您選擇"+adapter.getItem(position), Toast.LENGTH_LONG).show();
                    textView3.setText(date.isEmpty()?"":date.substring(0,6)+"-"+date.substring(6));
                }catch (Exception e){
                    Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Toast.makeText(MainActivity.this, "您沒有選擇任何項目", Toast.LENGTH_LONG).show();
            }
        });

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
                    if(items.size() != 0) {
                        textView.post(new Runnable() {
                            Boolean flag = false;
                            Boolean dateFound = false;
                            @Override
                            public void run() {
                                try {
                                    String t = "";
                                    StringBuilder tmp = new StringBuilder();
                                    if(debugMessage.isEmpty()){
                                        for(int i =0;i<items.size();++i) {
                                            TextBlock item = items.valueAt(i);
                                            matcher2 = pattern2.matcher(item.getValue());
                                            matcher3 = pattern3.matcher(item.getValue());
                                            matcher4 = pattern4.matcher(item.getValue());
                                            if(matcher4.find()){
                                                String in = matcher4.group().substring(0,4);
                                                flag = date.substring(5,8).equals(in.replaceAll("-",""));
                                                dateFound = !in.isEmpty();
                                            }
                                            if(matcher2.find()){
                                                t = matcher2.group().replaceAll(" ","");
                                                t = t.replaceAll("-","");
                                                break;
                                            }else if(matcher3.find()){
                                                t = matcher3.group();
                                                break;
                                            }
                                            textView.setText("發票號碼 : ");
                                            textView2.setText("");
                                        }
                                        for (int i = 2; i < t.length(); i++)
                                            tmp.append(t.charAt(i));
                                        t = tmp.toString();
                                        tmp = new StringBuilder(c.check(tmp.toString()));
                                        if(dateFound){
                                            if(flag){
                                                textView2.setTextSize(36);
                                                t = "發票號碼 : "+t +(tmp.toString().compareTo("請對齊發票")==0? tmp.toString() :"");
                                                textView.setText(t);
                                                textView2.setText((tmp.toString().compareTo("請對齊發票")==0?"": tmp.toString()));
                                            }else if((tmp.toString().compareTo("請對齊發票")!=0)){
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
                                }catch (Exception e){
                                    textView.setText(e.toString());
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}