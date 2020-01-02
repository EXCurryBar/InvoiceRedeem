package dev.edmt.androidcamerarecognitiontext;

import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class add extends Thread{
    private String InvNum;
    private String month;
    private String price;
    private SQLiteDatabase dbrw;
    public add(String invn, String m, String p, SQLiteDatabase db){
        InvNum = invn;
        month = m;
        price = p;
        dbrw = db;
    }
    public void run(){
        try{
            dbrw.execSQL("INSERT INTO myTable(InvNum, month, price) VALUES(?,?,?)",
                    new Object[]{InvNum, month, price});
            InvNum = "";
            month = "";
            price = "";
        }catch (Exception e){
            //Toast.makeText(MainActivity.this,"新增失敗"+e.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
