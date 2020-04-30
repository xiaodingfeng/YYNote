package com.example.xiaobai.yynote.util;

/*
将传入的String类型的便签内容，转化为包含图片 + 可点击声音的 spannableString
这里传入 note content（string） 其中格式如下 你好，<img src=''>， <voice src=''> 经过处理后 得到一个spannableString ，将其中的img he
voice setSpan变为两个标志，之后textView .set 就会将其还原
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.load.engine.Resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentToSpannableString {


    public static SpannableString Content2SpanStr(Context context, String noteContent,boolean check) {
        //这里的fakeNoteContent 是虚假content，是展示给用户的，因为真正的content中包含着的声音src变为可点击spannable之后会很丑
        String fakeNoteContent = noteContent;
        ArrayList<String> voiceSrc = new ArrayList<>();
        Pattern voice = Pattern.compile("<voice src='(.*?)'/>");
        Matcher mVoice = voice.matcher(fakeNoteContent);
        while(mVoice.find()){
            String str1 = mVoice.group(0);
            if(check)
                fakeNoteContent = fakeNoteContent.replace(str1,"");
            String str2 = mVoice.group(1);
            voiceSrc.add(str2);
        }

        Log.d("voiceSrc的大小",Integer.toString(voiceSrc.size()));

        Pattern img = Pattern.compile("<img src='(.*?)'/>");
        Matcher mImg = img.matcher(fakeNoteContent);

        // "\uD83C\uDFA4", 这是android手机的emoji录音图标
        Pattern voiceLogo = Pattern.compile("\uD83C\uDFA4");
        Matcher mVoiceLogo = voiceLogo.matcher(fakeNoteContent);

        SpannableString spanStr = new SpannableString(fakeNoteContent);

        while(mImg.find()){
            String str = mImg.group(0);
            int start = mImg.start();   int end = mImg.end();
            Uri imgUri = Uri.parse(mImg.group(1));
            Drawable drawable = null;
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            int x = point.x;
            int y = point.y;
            try {
                drawable = Drawable.createFromStream(context.getContentResolver().openInputStream(imgUri),null);
                drawable.setBounds(x/10,0,(x*9)/10,drawable.getIntrinsicHeight()*9*x/(drawable.getIntrinsicWidth()*10));

            } catch (FileNotFoundException e) {
                Resources resource=context.getResources();
                int resid=resource.getIdentifier("erros","drawable",context.getPackageName());
                drawable = resource.getDrawable(resid);
                drawable.setBounds(x/10,0,(x*9)/10,drawable.getIntrinsicHeight()*9*x/(drawable.getIntrinsicWidth()*10));
                e.printStackTrace();
            }
            ImageSpan imageSpan = new ImageSpan(drawable);

            spanStr.setSpan(imageSpan,start,end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        }

        int i = 0;
        while(mVoiceLogo.find()){

            Log.d("下标i",Integer.toString(i));
            int start = mVoiceLogo.start();     int end = mVoiceLogo.end();
            final String voiceFilePath = voiceSrc.get(i);
            i++;

            //可点击的SpannableString
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    //实现点击事件
                    Log.d("voice能否点击","能够点击");
                    MediaPlayer mp = new MediaPlayer();
                    try {
                        mp.setDataSource(voiceFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(mediaPlayer != null){
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }

                        }
                    });

                    try {
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mp.start();
                    //etc
                }
            };
            spanStr.setSpan(clickableSpan,start,end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            //不加下面这句点击没反应
            //textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        return spanStr;
    }



}
