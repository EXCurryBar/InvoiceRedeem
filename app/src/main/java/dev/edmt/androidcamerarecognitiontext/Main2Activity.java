package dev.edmt.androidcamerarecognitiontext;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    private Button btn_delete;
    private TextView textView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> items = new ArrayList<>();
    private SQLiteDatabase dbrw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btn_delete = (Button) findViewById(R.id.btn_delete);
        listView = (ListView) findViewById(R.id.listView);
        textView = (TextView) findViewById(R.id.textView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        dbrw = new MyDBHelper(this).getWritableDatabase();
        Cursor c;

        c = dbrw.rawQuery("SELECT * FROM myTable", null);

        c.moveToFirst();
        items.clear();
        int ii = c.getCount();
        Toast.makeText(Main2Activity.this, "共有" + ii + "筆資料", Toast.LENGTH_SHORT).show();
        textView.setText("共有 " + ii + " 張中獎發票");
        for(int i = 0; i < c.getCount(); i++){
            items.add("中獎號碼:" + c.getString(0) + "\n" + c.getString(1) + "\n金額:" + c.getString(2));
            c.moveToNext();
        }
        adapter.notifyDataSetChanged();
        c.close();

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    try{
                        dbrw.execSQL("DELETE FROM myTable");
                        Toast.makeText(Main2Activity.this,"刪除所有發票",Toast.LENGTH_SHORT).show();

                    }catch (Exception e){
                        Toast.makeText(Main2Activity.this,"刪除失敗"+e.toString(),Toast.LENGTH_LONG).show();
                    }
                Cursor c;

                c = dbrw.rawQuery("SELECT * FROM myTable", null);

                c.moveToFirst();
                items.clear();
                int ii = c.getCount();
                Toast.makeText(Main2Activity.this, "共有" + ii + "筆資料", Toast.LENGTH_SHORT).show();
                textView.setText("共有 " + ii + " 張中獎發票");
                for(int i = 0; i < c.getCount(); i++){
                    items.add("中獎號碼:" + c.getString(0) + "\n" + c.getString(1) + "\n金額:" + c.getString(2));
                    c.moveToNext();
                }
                adapter.notifyDataSetChanged();
                c.close();
            }
        });
    }
}
