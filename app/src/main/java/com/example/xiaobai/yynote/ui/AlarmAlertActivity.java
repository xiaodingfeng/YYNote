package com.example.xiaobai.yynote.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.example.xiaobai.yynote.R;

public class AlarmAlertActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();
        String noteContent = intent.getStringExtra("NoteContent");
        Log.d("传递的内容",noteContent);

        setContentView(R.layout.test);
        mediaPlayer = mediaPlayer.create(this,R.raw.cute);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        //创建一个闹钟提醒的对话框,点击确定关闭铃声与页面
        Log.d("进入新的","已经进入");


        new AlertDialog.Builder(AlarmAlertActivity.this).setTitle("便签提醒").setMessage(noteContent)
                .setPositiveButton("关闭闹铃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mediaPlayer.stop();
                        AlarmAlertActivity.this.finish();
                    }
                }).show();
    }

}
