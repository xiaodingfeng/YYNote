package com.example.xiaobai.yynote.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.xiaobai.yynote.R;
import com.example.xiaobai.yynote.bean.Note;
import com.example.xiaobai.yynote.db.NoteDbHelpBusiness;
import com.example.xiaobai.yynote.util.ContentToSpannableString;
import com.example.xiaobai.yynote.util.UriImageGetter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NoteActivity extends AppCompatActivity {
    Note note = null;
    TextView textView;
    String wordSizePrefs;
    private AlarmManager alarmManager;
    private PendingIntent pi;
    private long date1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_show);

        initData();

        Toolbar toolbar_note_show = (Toolbar)findViewById(R.id.toolbar_note_show);
        setSupportActionBar(toolbar_note_show);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar_note_show.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        textView = this.findViewById(R.id.TextView_showNote);
        float WordSize = getWordSize(wordSizePrefs);
        textView.setTextSize(WordSize);

        String content = note.getContent();

        /*
        if(content.contains("##")){
            //这说明便签中含有图片

        }else{
            textView.setText(content);
        }

        */

        //如果这个便签中包含图片



        //不能识别换行？？/n   replace 因为  Html.fromHtml 无法识别\n
        SpannableString spannableString = ContentToSpannableString.Content2SpanStr(NoteActivity.this, content);

        //不加下面这句点击没反应  可点击 字 的实现要求 注意：要位于textView.setText()的前面
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString);

        //textView.setText(Html.fromHtml(content.replace("\n", "<br/>"),new UriImageGetter(this),null));

        //textView.setText(note.getContent());
        //String str = textView.getText().toString();
        //Log.d("textView", "textView" + str);

        FloatingActionButton btn_note_complete = findViewById(R.id.button_note_edit);
        btn_note_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到新建页面，编辑意味着 删除原来的， 新建一个新的，只是新的这个的content继承自旧的。
                Intent intent = new Intent(NoteActivity.this, NoteNewActivity.class);
                //告诉 是编辑页面 editText需要继承旧的东西
                intent.putExtra("NewOrEdit","Edit");
                Bundle bundle = new Bundle();
                bundle.putSerializable("OldNote",note);
                intent.putExtra("data",bundle);
                intent.putExtra("groupName", note.getGroupName());
                startActivity(intent);
                finish();
                //editNote();
            }
        });
    }


    private void initData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note)bundle.getSerializable("Note");

        //字体大小默认是20dp  正常    其中 15 dp 对应小     25dp  对应 大    30dp对应超大
        SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
        wordSizePrefs = prefs.getString("WordSize","正常");
    }

    private void editNote(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_show_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.show_menu_delete) {
            final NoteDbHelpBusiness dbBus = NoteDbHelpBusiness.getInstance(this);
            if(note.getGroupName().equals("回收站")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                AlertDialog alertDialog = builder.setTitle("系统提示：")
                        .setMessage("确定要永久删除该便签吗？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                    dbBus.deleteNote(note);
                                finish();
                            }
                        }).create();
                alertDialog.show();
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                AlertDialog alertDialog = builder.setTitle("系统提示：")
                        .setMessage("确定要加入回收站吗？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbBus.UpdateGroupName(note,"回收站");
                                finish();
                            }
                        }).create();
                alertDialog.show();
            }
        }else if(id == R.id.show_menu_wordSize){
            final String[] wordSize = new String[]{"小","正常","大","超大"};
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            AlertDialog alertDialog = builder.setTitle("选择字体大小")
                                        .setSingleChoiceItems(wordSize, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                wordSizePrefs = wordSize[i];
                                                float WordSize = getWordSize(wordSizePrefs);
                                                textView.setTextSize(WordSize);
                                                SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("WordSize",wordSizePrefs);
                                                editor.apply();         //editor.commit();
                                            }
                                        }).create();
            alertDialog.show();
        }else if(id == R.id.show_menu_share){
            //由于qq，微信需要注册，所以暂时没弄 只能分享到系统自带的应用中
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, note.getContent());
            startActivity(intent.createChooser(intent,"分享到"));
        }else if (id == R.id.show_menu_remind){
            //后台 service
            //计时 AlarmManager
            //发送 notification
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(NoteActivity.this,AlarmReceiver.class);
            intent.putExtra("NoteContent",note.getContent());
            pi = PendingIntent.getBroadcast(NoteActivity.this,0,intent,0);
            setReminder();
        }
        else if(id==R.id.show_menu_remindcancel){
            if(alarmManager!=null)
            alarmManager.cancel(pi);
            Toast.makeText(NoteActivity.this, "取消提醒成功！", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //由于调整了字体大小，所以重新载入Note
    private void reloadNote(){

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

    private void setReminder(){
//提交测试

        //不知道是什么鬼原理，方正成功了，，醉了，下面的dialog的构造函数的System.currentTimeMillis() 就是 显示在用户dialog上面的基础时间
        DateTimePickerDialog d = new DateTimePickerDialog(this,System.currentTimeMillis());
        d.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void OnDateTimeSet(android.app.AlertDialog dialog, long date) {
                date1 = date;
                alarmManager.set(AlarmManager.RTC_WAKEUP, date1,pi);
                Toast.makeText(NoteActivity.this, "设置提醒成功！", Toast.LENGTH_SHORT).show();
                Log.d("时间","时间是" + date1);
            }
        });
        d.show();
    }

}
