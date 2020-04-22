package com.example.xiaobai.yynote.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.xiaobai.yynote.R;
import com.example.xiaobai.yynote.bean.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class Meiwen extends AppCompatActivity {
    private Handler handler;
    private List<Article> newsList;
    private TextView textView;
    private TextView textView1;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() !=null) {

            getSupportActionBar().hide();
        }
        setContentView(R.layout.meiweneveryday);
        textView=(TextView)findViewById(R.id.textView2);
        textView1=(TextView)findViewById(R.id.textView4);
        getNews();
        handler = new Handler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                textView.setText("handleMessage。。。");
                textView1.setText("handleMessage。。。");
                if (msg.what == 1) {
                    Article news = newsList.get(0);
                    textView.setText(news.getArticleTitle()+"\n"+news.getArticleWho());
                    textView1.setText(news.getContents());
                }
                else{
                    textView.setText("加载中。。。");
                    textView1.setText("加载中。。。");
                }
            }
        };
    }

    private void getNews(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                   int i= (int) (Math.random() * 36);
                    textView.setText("加载中。。。");
                    textView1.setText("加载中。。。");
                   Document doc = Jsoup.connect("http://www.xiaole8.com/renshengzheli/page_"+Integer.toString(i)+".html").get();
                   Elements titleLinks = doc.select("ul.l2");    //解析来获取每条新闻的标题与链接地址
                    Elements titlelins = titleLinks.get(0).select("li");
                    int j= (int) (Math.random() * titlelins.size());
                    String uri = titlelins.get(j).select("a").attr("href");
                    String title = titlelins.get(j).select("a").text();
                    Document doc1 = Jsoup.connect(uri).get();
                    String mainarctile=doc1.select("div.wzcon").select("p").text();
                    String laiyuan=doc1.select("div.info").text();
                    mainarctile=mainarctile.replaceAll("<br>","\n");
                    Article article = new Article(title, uri,laiyuan,mainarctile);
                    newsList.add(article);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);

                }catch (Exception e){
                   e.printStackTrace();
                }
            }
        }).start();
    }
}
