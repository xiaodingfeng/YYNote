package com.example.xiaobai.yynote.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiaobai.yynote.R;
import com.example.xiaobai.yynote.bean.Note;
import com.example.xiaobai.yynote.db.NoteDbHelpBusiness;
import com.example.xiaobai.yynote.util.ContentToSpannableString;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Locale;


public class NoteActivity extends AppCompatActivity {
    Note note = null;
    TextView textView;
    String wordSizePrefs;
    private Context mContext;
    private AlarmManager alarmManager;
    private PendingIntent pi;
    private long date1;
    private  int notegroupid=0;
    private int REQUEST_PERMISSION_CODE;
    FloatingActionButton btn_note_complete;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_show);
        Window window = this.getWindow();
        //清除透明状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //设置状态栏颜色必须添加
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);//设置透明
        initData();

        Toolbar toolbar_note_show = (Toolbar)findViewById(R.id.toolbar_note_show);
        setSupportActionBar(toolbar_note_show);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setElevation(0);
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


        //如果这个便签中包含图片

        //不能识别换行？？/n   replace 因为  Html.fromHtml 无法识别\n
        SpannableString spannableString = ContentToSpannableString.Content2SpanStr(NoteActivity.this, content,true);

        //不加下面这句点击没反应  可点击 字 的实现要求 注意：要位于textView.setText()的前面
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString);

        btn_note_complete = findViewById(R.id.button_note_edit);
        if(notegroupid==3)
            btn_note_complete.setVisibility(View.GONE);
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
        if(note.getGroupName().equals("生活"))
            notegroupid=1;
        else if(note.getGroupName().equals("工作"))
            notegroupid=2;
        else if(note.getGroupName().equals("回收站"))
            notegroupid=3;
        //字体大小默认是20dp  正常    其中 15 dp 对应小     25dp  对应 大    30dp对应超大
        SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
        wordSizePrefs = prefs.getString("WordSize","正常");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_show_menu,menu);
        return true;
    }


    public Bitmap viewSaveToImage(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);

// 把一个View转换成图片
        Bitmap cachebmp = loadBitmapFromView(view);

// 添加水印
        Bitmap bitmap = Bitmap.createBitmap(createWatermarkBitmap(cachebmp,
                "@ YYNote"));

//        FileOutputStream fos;
//        try {
//// 判断手机设备是否有SD卡
//            boolean isHasSDCard = Environment.getExternalStorageState().equals(
//                    android.os.Environment.MEDIA_MOUNTED);
//            if (isHasSDCard) {
//// SD卡根目录
//                File sdRoot = Environment.getExternalStorageDirectory();
//                File file = new File(sdRoot, DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) +".PNG");
//                fos = new FileOutputStream(file);
//            } else
//                throw new Exception("创建文件失败!");
//
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
//
//            fos.flush();
//            fos.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        view.destroyDrawingCache();
        return bitmap;
    }

    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
/** 如果不设置canvas画布为白色，则生成透明 */

        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }

    // 为图片target添加水印
    private Bitmap createWatermarkBitmap(Bitmap target, String str) {
        int w = target.getWidth();
        int h = target.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint p = new Paint();

// 水印的颜色
        p.setColor(Color.RED);

// 水印的字体大小
        p.setTextSize(40);

        p.setAntiAlias(true);// 去锯齿

        canvas.drawBitmap(target, 0, 0, p);

// 在中间位置开始添加水印
        canvas.drawText(str, w / 2, h / 2, p);

        canvas.save();
        canvas.restore();

        return bmp;
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
        }
        else if(id==R.id.MoveGroup){
            final NoteDbHelpBusiness dbBus = NoteDbHelpBusiness.getInstance(this);
            final String[] items3 = new String[]{"未分组", "生活", "工作","回收站"};//创建item
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            AlertDialog alertDialog = builder.setTitle("移动地址：")
                    .setSingleChoiceItems(items3,notegroupid, new DialogInterface.OnClickListener() {//添加列表
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            notegroupid=i;
                            if(notegroupid==3)
                                btn_note_complete.setVisibility(View.GONE);
                            else
                                btn_note_complete.setVisibility(View.VISIBLE);
                            dbBus.UpdateGroupName(note,items3[i]);
                        }
                    }).create();
            alertDialog.show();
        }
        else if(id == R.id.show_menu_wordSize){
            final String[] wordSize = new String[]{"小","正常","大","超大"};
            final int[] index = {0};
            SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);

            if(prefs.getString("WordSize", "正常").equals("正常"))
                index[0] =1;
            else  if(prefs.getString("WordSize", "正常").equals("大"))
                index[0] =2;
            else  if(prefs.getString("WordSize", "正常").equals("超大"))
                index[0] =3;
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            AlertDialog alertDialog = builder.setTitle("选择字体大小")
                                        .setSingleChoiceItems(wordSize, index[0], new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                wordSizePrefs = wordSize[i];
                                                index[0] =i;
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

            AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
            AlertDialog alertDialog = builder.setTitle("选择分享方式：")
                    .setNegativeButton("图片", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(NoteActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                ActivityCompat.requestPermissions(NoteActivity.this,
                                        new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_CODE);
                            }
                            else{
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                Uri uriToImage = Uri.parse(MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), viewSaveToImage(textView), null, null));
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_STREAM, uriToImage);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent.createChooser(intent, "分享到"));

                            }
                                }
                    })
                    .setPositiveButton("文本", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, note.getContent());
                            startActivity(intent.createChooser(intent,"分享到"));
                        }
                    }).create();
            alertDialog.show();
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
