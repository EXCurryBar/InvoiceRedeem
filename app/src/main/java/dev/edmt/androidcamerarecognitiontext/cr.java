package dev.edmt.androidcamerarecognitiontext;

import android.annotation.SuppressLint;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;


public class cr extends Thread {
    Pattern p1 = Pattern.compile("\\d{8}");
    Pattern p2 = Pattern.compile("\\d{3}");
    Pattern p3 = Pattern.compile("<meta name=\"description\" content=.* />");
    Pattern p4 = Pattern.compile("統一發票.* 特別獎");
    private String r;
    private  int month =0;
    private  int position =0;
    public  cr(int m,int p){
        month=m;
        position=p;
    }
    public void run() {

        try {
            URL url = new URL("https://bluezz.com.tw/" + process(month,position) + ".php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                while ((r = reader.readLine()) != null) {
                    Matcher matcher = p3.matcher(r);
                    if(matcher.find()){
                        MainActivity.gottenMessage = matcher.group();
                    }
                }
            } else {
                MainActivity.debugMessage+="伺服器響應代碼為：" + responseCode+"\n";
            }
        } catch (Exception e) {
            MainActivity.debugMessage+=e.toString()+"\n";
        }
        Matcher matcher = p1.matcher(MainActivity.gottenMessage);
        Matcher matcher2 = p2.matcher(MainActivity.gottenMessage);
        Matcher matcher3 = p4.matcher(MainActivity.gottenMessage);
        int count = 0;
        boolean flag = false;
        while (matcher.find()) {
            MainActivity.EightNum[count] = matcher.group();
            ++count;
        }
        count = 0;
        int tmp = 0;
        while (matcher2.find()) {
            flag = matcher2.group().equals("200") != flag;
            if (count > 11 && flag) {
                MainActivity.ThreeNum[count - 12] = matcher2.group();
                tmp = count-12;
            }
            ++count;
        }
        count=tmp;
        int c = 2;
        for (int i = count; i < count+3; i++) {
            MainActivity.ThreeNum[i]=MainActivity.EightNum[c].substring(5);
            ++c;
        }
        if(matcher3.find()){
            MainActivity.date = matcher3.group().substring(4,8);
            MainActivity.date += process(month,position)+"月";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String process(int m, int position) {
        Calendar calendar = Calendar.getInstance();
        int date = calendar.get(Calendar.DATE);
        String s = "";
        if (position == 1) {
            if (m < 13 && m > 4) {
                if (m % 2 == 0) {
                    s = String.format("%02d%02d", m - 5, m - 4);
                } else {
                    if (date >= 25) {
                        s = String.format("%02d%02d", m - 4, m - 3);
                    }
                }
            } else if (m == 2 || m == 1) {
                s = (date >= 25 || m == 2) ? "0910" : "";
            }
            return s;
        }
        else{
            if (m < 13 && m > 4) {
                if (m % 2 == 0) {
                    s = String.format("%02d%02d", m - 3, m - 2);
                } else {

                    if(date>=25) {
                        s = String.format("%02d%02d", m - 2, m - 1);
                    }else
                    if (m==3)
                        s = "1112";
                    else
                        s = String.format("%02d%02d", m - 4, m - 3);
                }
            } else if (m == 2 || m == 1) {
                s = (date>=25||m==2)?"1112":"0910";
            }
            return s;
        }
    }

    String check(String invoice) {
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
