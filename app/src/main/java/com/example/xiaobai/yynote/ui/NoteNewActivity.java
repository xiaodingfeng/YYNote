package com.example.xiaobai.yynote.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.xiaobai.yynote.R;
import com.example.xiaobai.yynote.bean.Note;
import com.example.xiaobai.yynote.db.NoteDbHelpBusiness;
import com.example.xiaobai.yynote.util.CommonUtil;
import com.example.xiaobai.yynote.util.ContentToSpannableString;
import com.example.xiaobai.yynote.util.GlideImageEngine;
import com.example.xiaobai.yynote.util.UriToPathUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteNewActivity extends AppCompatActivity {
    private String NewOrEdit;
    Note oldNote = null;
    private String groupName;
    private EditText editText;
    private List<Uri> mSelected;
    private GlideImageEngine glideImageEngine;
    private int REQUEST_CODE_CHOOSE = 23;
    private boolean isStart = false;      //判断是否开始录音
    private MediaRecorder mediaRecorder = null;
    private int REQUEST_PERMISSION_CODE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_new);

        ActivityCompat.requestPermissions(NoteNewActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION_CODE);


        //初始化一些变量
        initData();

        Toolbar toolbar_note_new = (Toolbar)findViewById(R.id.toolbar_note_new);
        setSupportActionBar(toolbar_note_new);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar_note_new.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                    //向数据库中新增一条Note数据
                    closeSoftKeyInput();
                    editLoseFocus();
                    AddNote();
                finish();
            }
        });

        editText = findViewById(R.id.note_new_editText);
        if(NewOrEdit.equals("New")){

        }else{
            Bundle bundle = getIntent().getBundleExtra("data");
            oldNote = (Note)bundle.getSerializable("OldNote");
            SpannableString spannableString = ContentToSpannableString.Content2SpanStr(NoteNewActivity.this, oldNote.getContent());
            editText.append(spannableString);
            /*
            此时如果用户只是进来看一眼，就不应该删除。
            NoteDbHelpBusiness dbHelpBusiness = NoteDbHelpBusiness.getInstance(this);
            //编辑意味着将旧的便签删除
            dbHelpBusiness.deleteNote(oldNote);
            */
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        //打开这个activity的时候自动获得焦点 + 自动打开软键盘  当editText获得焦点的时候，软键盘就会打开，相当于你点了一下屏幕
        editGetFocus();




        FloatingActionButton btn_note_complete = findViewById(R.id.button_note_new_complete);
//        btn_note_complete.setVisibility(View.GONE);
        btn_note_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //向数据库中新增一条Note数据
                closeSoftKeyInput();
                editLoseFocus();
                AddNote();
                finish();
            }
        });

        final FloatingActionButton addPic = findViewById(R.id.button_note_new_picture);
//        addPic.setVisibility(View.GONE);
        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到 图片选择界面 向当前editText中插入图片
                callGallery();
            }
        });

        final Button addVoice = findViewById(R.id.button_note_new_voice);
//        addVoice.setVisibility(View.GONE);
        addVoice.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(!isStart){
                    startRecord();
                    addVoice.setText("停止录音");
                    isStart = true;
                    editText.append("\n");
                }else{
                    stopRecord();
                    addVoice.setText("开始录音");
                    isStart = false;
                    //这是手机emoji上的一个图标
                    editText.append("\uD83C\uDFA4");
                    editText.append("\n");
                }
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
//                    addPic.setVisibility(View.VISIBLE);
//                    addVoice.setVisibility(View.VISIBLE);
                    Log.d("焦点","获得焦点");
                }else{
//                    addPic.setVisibility(View.GONE);
//                    addVoice.setVisibility(View.GONE);
                    Log.d("焦点","失去焦点");
                }
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!editText.getText().toString().equals("")) {
                //向数据库中新增一条Note数据
                closeSoftKeyInput();
                editLoseFocus();
                AddNote();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    private void initData(){
        groupName = getIntent().getStringExtra("groupName");
        Log.d("groupName","传过来的组名是" + groupName);
        if(groupName.equals("全部")){
            groupName = "未分组";
        }

        NewOrEdit = getIntent().getStringExtra("NewOrEdit");
    }

    private void AddNote(){
        String mContent = editText.getText().toString();
        if(!mContent.equals("")) {
            String Contents = mContent;
            int i = 0;
            String title;
            Pattern voice = Pattern.compile("<voice src='(.*?)'/>");
            Matcher mVoice = voice.matcher(Contents);
            Contents = mVoice.replaceAll("");
            Pattern img = Pattern.compile("<img src='(.*?)'/>");
            Matcher mImg = img.matcher(Contents);
            Contents = mImg.replaceAll("");
            int k = 0;
            for (i = 0; i < Contents.length(); i++) {
                if (Contents.charAt(i) == '\n') {
                    if (!Contents.substring(k, i).trim().equals("")) {
                        break;
                    }
                    k = i;
                }
            }
            title = Contents.substring(k, i);

            String subContent;
            if (i < Contents.length()) {
                int j = 0;
                for (j = i + 1; j < Contents.length(); j++) {
                    if (Contents.charAt(j) == '\n') {
                        if (!Contents.substring(i + 1, j).trim().equals(""))
                            break;
                    }
                }
                subContent = Contents.substring(i + 1, j);
            } else {
                subContent = "";

            }

            Log.d("mContent:", "用户输入的内容是" + mContent);
            Note note = new Note();
            note.setTitle(title);
            note.setSubContent(subContent);
            note.setContent(mContent);
            note.setCreateTime(CommonUtil.date2string(new Date()));
            note.setGroupName(groupName);

            NoteDbHelpBusiness dbBus = NoteDbHelpBusiness.getInstance(this);

            //当用户确定完成编辑之后， 意味着将旧的便签删除
            if (oldNote != null) {
                dbBus.deleteNote(oldNote);
            }

            dbBus.addNote(note);
        }
    }

    //关闭软键盘
    private void closeSoftKeyInput() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void editLoseFocus(){
        editText.clearFocus();
    }

    private void editGetFocus(){
        editText.requestFocus();
    }

    private void callGallery(){
        glideImageEngine = new GlideImageEngine();

        Matisse.from(NoteNewActivity.this)
                .choose(MimeType.ofAll())
                .countable(true)
                .maxSelectable(9)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(glideImageEngine)
                .forResult(REQUEST_CODE_CHOOSE);
//        Intent intentFromGallery = new Intent();
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {//4.4及以上
//            intentFromGallery.setAction(Intent.ACTION_PICK);
//        } else {//4.4以下
//            intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
//        }
//        intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        startActivityForResult(intentFromGallery, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            if(data != null){
                if(requestCode == 1){

                }else if(requestCode == REQUEST_CODE_CHOOSE){
                    mSelected = Matisse.obtainResult(data);
                    Uri nSelected = mSelected.get(0);
//                    Uri nSelected =data.getData();
                    //用Uri的string来构造spanStr，不知道能不能获得图片
                    //  ## +  string +  ##  来标识图片  <img src=''>

                    //SpannableString spanStr = new SpannableString(nSelected.toString());
                    SpannableString spanStr = new SpannableString("<img src='" + nSelected.toString() + "'/>");
                    Log.d("图片Uri",nSelected.toString());
//                    String path = UriToPathUtil.getRealFilePath(this,nSelected);
//                    Log.d("图片Path",path);

                    try{
                        WindowManager windowManager = (WindowManager) (NoteNewActivity.this).getSystemService(Context.WINDOW_SERVICE);
                        Display defaultDisplay = windowManager.getDefaultDisplay();
                        Point point = new Point();
                        defaultDisplay.getSize(point);
                        int x = point.x;
                        int y = point.y;
                        //根据Uri 获得 drawable资源
                        //x/t=y/z
                        Drawable drawable = Drawable.createFromStream(this.getContentResolver().openInputStream(nSelected),null);
                        drawable.setBounds(x/10,0,x*9/10,drawable.getIntrinsicHeight()*9*x/(drawable.getIntrinsicWidth()*10));
                        //BitmapDrawable bd = (BitmapDrawable) drawable;
                        //Bitmap bp = bd.getBitmap();
                        //bp.setDensity(160);
                        ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BASELINE);
                        spanStr.setSpan(span,0,spanStr.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.d("spanString：",spanStr.toString());
                        int cursor = editText.getSelectionStart();
                        editText.getText().insert(cursor, spanStr);
                        int cursor1=editText.getSelectionEnd();
                        editText.setSelection(cursor1);//将光标移至文字末尾
                        editText.getText().insert(cursor1,"\n");
                        int cursor2=editText.getSelectionEnd();
                        editText.setSelection(cursor2);//将光标移至文字末尾
                        editText.requestFocus();//获取焦点
                    }catch (Exception FileNotFoundException){
                        Log.d("异常","无法根据Uri找到图片资源");
                    }
                    //Drawable drawable = NoteNewActivity.this.getResources().getDrawable(nSelected);
                }
            }
        }
    }
    @SuppressLint("SdCardPath")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startRecord(){


        if(mediaRecorder == null){
            boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            String rootDir;
            if (hasSDCard) {
                rootDir= Environment.getExternalStorageDirectory().toString();
            } else
                rootDir= "/data/data";
            File dir = new File(rootDir,"sounds");
            if (!dir.exists()){
                dir.mkdirs();
            }
            File soundFile = new File(dir, DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA))  + ".m4a");
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(soundFile.getPath());
            editText.append("<voice src='" + soundFile.getPath() + "'/>");

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void stopRecord(){
        if (mediaRecorder != null){
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (IllegalStateException e) {
                mediaRecorder = null;
                e.printStackTrace();
            } catch (RuntimeException e) {
                mediaRecorder = null;
                e.printStackTrace();
            } catch (Exception e) {
                mediaRecorder = null;
                e.printStackTrace();
            }

        }
    }

}
