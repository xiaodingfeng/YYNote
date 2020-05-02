package com.example.xiaobai.yynote.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiaobai.yynote.R;
import com.example.xiaobai.yynote.bean.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Meiwen extends AppCompatActivity {
    private Handler handler;
    private static Article article=new Article();
    private TextView textView;
    private TextView textView1;
    private TextView textView2;
    String wordSizePrefs;
    private  float pressX,pressY,moveX,moveY;
    private String urlBefor;
    private String urlNext;
    private String urlMeiri="https://meiriyiwen.com/random";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"HandlerLeak", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() !=null) {

            getSupportActionBar().hide();
        }
        setContentView(R.layout.meiweneveryday);
        Window window = this.getWindow();
        //清除透明状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //设置状态栏颜色必须添加
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);//设置透明
        //设置状态栏文字颜色及图标为深色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        textView=(TextView)findViewById(R.id.textView2);
        textView1=(TextView)findViewById(R.id.textView4);
        textView2=(TextView)findViewById(R.id.textView3);
        RelativeLayout relativeLayout=(RelativeLayout)findViewById(R.id.meiwen);

        textView1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String[] wordSize = new String[]{"小","正常","大","超大"};
                final int[] index = {0};
                SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);

                if(prefs.getString("WordSizeToMeiwen", "正常").equals("正常"))
                    index[0] =1;
                else  if(prefs.getString("WordSizeToMeiwen", "正常").equals("大"))
                    index[0] =2;
                else  if(prefs.getString("WordSizeToMeiwen", "正常").equals("超大"))
                    index[0] =3;
                AlertDialog.Builder builder = new AlertDialog.Builder(Meiwen.this);
                AlertDialog alertDialog = builder.setTitle("选择字体大小")
                        .setSingleChoiceItems(wordSize, index[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                wordSizePrefs = wordSize[i];
                                index[0] =i;
                                float WordSize = getWordSize(wordSizePrefs);
                                textView1.setTextSize(WordSize);
                                SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("WordSizeToMeiwen",wordSizePrefs);
                                editor.apply();         //editor.commit();
                            }
                        }).create();
                alertDialog.show();
                return false;
            }
        });
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("WrongConstant")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    //按下
                    case MotionEvent.ACTION_DOWN:
                        pressX = event.getX();
                        pressY = event.getY();
                        break;
                    //移动
                    case MotionEvent.ACTION_MOVE:
                        moveX = event.getX();
                        moveY = event.getY();
                        break;
                    //松开
                    case MotionEvent.ACTION_UP:
                        if (moveX-pressX > 0 && Math.abs(moveY - pressY) < 250) {
                            getNews(urlMeiri);
                        } else if (moveX - pressX < 0 && Math.abs(moveY - pressY) < 250) {
                            getNews(urlMeiri);
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        Typeface typeFace =Typeface.createFromAsset(getAssets(), "fonts/fzfsk.ttf");
        textView.setTypeface(typeFace);
        textView1.setTypeface(typeFace);
        textView2.setTypeface(typeFace);
        textView.setText("\n\n\n\n\n\n\n\n\n\n"+"正在加载！");
        textView1.setText("");
        textView2.setText("");
        getNews(urlMeiri);
        SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
        wordSizePrefs = prefs.getString("WordSizeToMeiwen","正常");
        float WordSize = getWordSize(wordSizePrefs);
        textView1.setTextSize(WordSize);
        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint({"SetTextI18n", "WrongConstant"})
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
//                    textView1.setFocusable(0);
                    final ScrollView vScrollView=(ScrollView)findViewById(R.id.scrollView) ;
                    vScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            vScrollView.fullScroll(View.FOCUS_UP);
                        }
                    });
                    textView.setText(article.getArticleTitle());
                    textView2.setText(article.getArticleWho());
                    textView1.setText(article.getContents()+"\n\n全文完，字数："+article.getContents().length()+"\n\n");
                }
                else if(msg.what==0){
                    textView.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n"+"访问数据超时！");
                    textView2.setText("");
                    textView1.setText("");
                }
            }
        };
    }

    private float getWordSize(String str){
        if (str.equals("小")){
            return 15;
        }else if(str.equals("正常")){
            return 20;
        }else if(str.equals("大")) {
            return 25;
        }else if(str.equals("超大")) {
            return 30;
        }
        return 20;
    }


    public static Document getJsoupDocGet(String url) {
        //三次试错
        final int MAX = 10;
        int time = 0;
        Document doc = null;
        while (time < MAX) {
            try {
                doc = Jsoup
                        .connect(url)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .timeout(1000 * 30)
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36")
                        .header("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("accept-encoding","gzip, deflate, br")
                        .header("accept-language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                        .get();
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                time++;
            }
        }
        return doc;
    }
    private void getNews(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //每日一文文章网
                    Document doc3 =getJsoupDocGet(url);
                    if(doc3!=null) {
                        Element arcticle = doc3.selectFirst("div#article_show");
                        String Title = arcticle.selectFirst("h1").text();
                        String Who = arcticle.selectFirst("p.article_author").text();
                        String Contents = arcticle.selectFirst("div.article_text").html();
                        Pattern pattern = Pattern.compile("</p>");
                        Matcher matcher = pattern.matcher(Contents);
                        String newString = matcher.replaceAll("\n");
                        Pattern pattern1 = Pattern.compile("<p>");
                        Matcher matcher1 = pattern1.matcher(newString);
                        newString = matcher1.replaceAll("    ");
                        article.setArticleTitle(Title);
//                    article.setArticleUrl(uri);
                        article.setArticleWho(Who);
                        article.setArticleContent(newString);
                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                    else{
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                }catch (Exception e){
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                   e.printStackTrace();
                }
            }
        }).start();
    }
}
