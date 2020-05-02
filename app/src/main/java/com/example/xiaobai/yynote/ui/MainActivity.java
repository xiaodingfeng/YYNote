package com.example.xiaobai.yynote.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.xiaobai.yynote.util.GlideImageEngine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiaobai.yynote.R;
import com.example.xiaobai.yynote.adapter.NoteAdapter;
import com.example.xiaobai.yynote.bean.Note;
import com.example.xiaobai.yynote.bean.WeatherApi;
import com.example.xiaobai.yynote.db.NoteDbHelpBusiness;
import com.example.xiaobai.yynote.util.NetworkUtil;
import com.example.xiaobai.yynote.view.SpacesItemDecoration;
import com.simple.spiderman.CrashModel;
import com.simple.spiderman.SpiderMan;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,View.OnClickListener {
    NoteAdapter noteAdapter;
    NoteDbHelpBusiness dbBus;
    NetworkUtil networkUtil;
    private Handler handler;
//    WeatherInfo weatherInfo;
    private LinkedList<Note> notes;
    private String groupName = "全部";
    public  Toolbar toolbar_main;
    ImageView imageView;
    String showNotesModel;
    RecyclerView recyclerView;
    private GlideImageEngine glideImageEngine;
    private int REQUEST_CODE_CHOOSE = 23;
    private int REQUEST_PERMISSION_CODE;
    public void onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"SetTextI18n", "HandlerLeak", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        //清除透明状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //设置状态栏颜色必须添加
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);//设置透明
//        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        //弹出崩溃信息展示界面
        SpiderMan.getInstance()
                .init(this)
                //设置是否捕获异常，不弹出崩溃框
                .setEnable(true)
                //设置是否显示崩溃信息展示页面
                .showCrashMessage(true)
                //是否回调异常信息，友盟等第三方崩溃信息收集平台会用到,
                .setOnCrashListener(new SpiderMan.OnCrashListener() {
                    @Override
                    public void onCrash(Thread t, Throwable ex, CrashModel model) {
                        //CrashModel 崩溃信息记录，包含设备信息
                    }
                });
        if(!NetworkUtil.isNetworkConnected(this))
            Toast.makeText(this, "网络不可用", 0).show();
        else
            Toast.makeText(this, "网络可用", 0).show();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION_CODE);
         toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        CoordinatorLayout coordinatorLayout=(CoordinatorLayout)findViewById(R.id.layout1);
        SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
        if(prefs.getString("imageuri", null)==null)
        coordinatorLayout.setBackground(getResources().getDrawable(R.drawable.back2));
        else{
            Uri nSelected=Uri.parse(prefs.getString("imageuri", null));
            try {
                Drawable drawable = Drawable.createFromStream(this.getContentResolver().openInputStream(nSelected), null);
                coordinatorLayout.setBackground(drawable);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        //用户喜好  首页面 notes布局显示  第一次默认 宫格模式
        showNotesModel = prefs.getString("ShowNotesModel","宫格模式");
        SearchView searchView =  findViewById(R.id.search_view);
        searchView.bringToFront();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                refreshNotes();
                notes = filter(notes,s);
                refreshAdapter();
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                refreshNotes();
                refreshAdapter();
                return false;
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(MainActivity.this, NoteNewActivity.class);
                    intent.putExtra("groupName",groupName);
                    intent.putExtra("NewOrEdit","New");
                    startActivity(intent);
            }
        });

        dbBus = NoteDbHelpBusiness.getInstance(this);
        refreshNotes();
        noteAdapter = new NoteAdapter(notes,this);
        noteAdapter.setOnItemClickListener(new NoteAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Note", note);
                    intent.putExtra("data", bundle);
                    startActivity(intent);
            }
        });
        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                if(groupName.equals("回收站")) {

                    final String[] items3 = new String[]{"未分组", "生活", "工作"};//创建item
                    //弹出一个dialog，用用户选择是否删除
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    AlertDialog alertDialog1 = builder1.setTitle("系统提示：")
                            .setMessage("永久删除？恢复？")
                            .setNegativeButton("恢复", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    AlertDialog alertDialog = builder.setTitle("恢复地址：")
                                            .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dbBus.UpdateGroupName(note,items3[i]);
                                                    refreshNotes();
                                                    refreshAdapter();
                                                }
                                    }).create();
                                    alertDialog.show();
                                }
                            })
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dbBus.deleteNote(note);
                                    refreshNotes();
                                    refreshAdapter();
                                }
                            }).create();
                    alertDialog1.show();
                }
                else {
                //弹出一个dialog，用用户选择是否删除
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                                                        refreshNotes();
                                                        refreshAdapter();
                                                }
                                            }).create();
                alertDialog.show();
            }
        }
        });

        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.addItemDecoration(new SpacesItemDecoration(3));   //用了card view之后不用再设置分隔线了 不知道上面那句要不要去掉
        recyclerView.setAdapter(noteAdapter);
        //根据用户的喜好设置 来刷新 LayoutManager，完成实时刷新
        refreshLayoutManager();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar_main, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //调用getHeaderView方法获得Header
        View headerView = navigationView.getHeaderView(0);
        final TextView textView1=(TextView)headerView.findViewById(R.id.textView2);//date
        final TextView textView2=(TextView)headerView.findViewById(R.id.textView1);//city
        final TextView textView3=(TextView)headerView.findViewById(R.id.textView);//wen
        final TextView textView4=(TextView)headerView.findViewById(R.id.textView3);//Pm
        final TextView textView5=(TextView)headerView.findViewById(R.id.textView4);//now
        final TextView textView6=(TextView)headerView.findViewById(R.id.textView5);//Tianqi
        timer.schedule(task,0,60000);
        handler = new Handler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    textView1.setText("日期："+(WeatherApi.weatherInfo.getDate()==null?"获取失败":WeatherApi.weatherInfo.getDate()));
                    textView2.setText("城市："+(WeatherApi.weatherInfo.getCityname()==null?"获取失败":WeatherApi.weatherInfo.getCityname()));
                    textView3.setText("气温："+(WeatherApi.weatherInfo.getTemperature()==null?"获取失败":WeatherApi.weatherInfo.getTemperature()));
                    textView4.setText("PM2.5指数："+(WeatherApi.weatherInfo.getAirquality()==null?"获取失败":WeatherApi.weatherInfo.getAirquality()));
                    textView5.setText("实时温度："+(WeatherApi.weatherInfo.gettemperatureNow()==null?"获取失败":WeatherApi.weatherInfo.gettemperatureNow()));
                    textView6.setText("天气："+(WeatherApi.weatherInfo.getWeather()==null?"获取失败":WeatherApi.weatherInfo.getWeather()));
                }
                else if(msg.what == 0){
                    textView1.setText("日期："+"获取超时");
                    textView2.setText("城市："+"获取超时");
                    textView3.setText("气温："+"获取超时");
                    textView4.setText("PM2.5指数："+"获取超时");
                    textView5.setText("实时温度："+"获取超时");
                    textView6.setText("天气："+"获取超时");
                }

            }
        };
    }
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                WeatherApi wh = new WeatherApi("https://www.toutiao.com/stream/widget/local_weather/data/", "http://whois.pconline.com.cn/");
                if (wh.JsonWeather()) {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                else{
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }
    };

    // 根据组名groupName 刷新数据  notes 对象 ，由于groupName的变化，或者其他增删导致 数据变化 ,合理并不会对搜索框的过滤刷新
    private void refreshNotes(){
        if(groupName.equals("全部")){
            notes = dbBus.getAll();
        }else{
            notes = dbBus.getNotesByGroup(groupName);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    //返回后台运行
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.list_mode) {
            showNotesModel = "列表模式";
            SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ShowNotesModel",showNotesModel);
            editor.apply();         //editor.commit();
            refreshLayoutManager();
        }else if(id == R.id.grid_model){
            showNotesModel = "宫格模式";
            SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ShowNotesModel",showNotesModel);
            editor.apply();         //editor.commit();
            refreshLayoutManager();
        }else if(id==R.id.Article){
            Intent intent = new Intent(MainActivity.this, Meiwen.class);
            startActivity(intent);
        }else if(id==R.id.backgroudimage){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog alertDialog = builder.setTitle("请选择：")
                    .setNegativeButton("默认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CoordinatorLayout coordinatorLayout=(CoordinatorLayout)findViewById(R.id.layout1);
                            coordinatorLayout.setBackground(getResources().getDrawable(R.drawable.back2));
                            SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("imageuri",null);
                            editor.apply();         //editor.commit();
                        }
                    })
                    .setPositiveButton("自定义", new DialogInterface.OnClickListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_CODE);
                            }
                            else{
                                //知乎开源项目打开图片
                                glideImageEngine = new GlideImageEngine();

                                Matisse.from(MainActivity.this)
                                        .choose(MimeType.ofAll())
                                        .countable(true)
                                        .maxSelectable(9)
                                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                        .thumbnailScale(0.85f)
                                        .imageEngine(glideImageEngine)
                                        .forResult(REQUEST_CODE_CHOOSE);
                            }
                            }
                    }).create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            if(data != null){
                if(requestCode == 1){

                }else if(requestCode == REQUEST_CODE_CHOOSE){
                    Uri nSelected = Matisse.obtainResult(data).get(0);
                    try{
                        Drawable drawable = Drawable.createFromStream(this.getContentResolver().openInputStream(nSelected),null);
                        CoordinatorLayout coordinatorLayout=(CoordinatorLayout)findViewById(R.id.layout1);
                        coordinatorLayout.setBackground(drawable);
                        SharedPreferences prefs = getSharedPreferences("Setting",MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("imageuri",nSelected.toString());
                        editor.apply();         //editor.commit();
                    }catch (Exception FileNotFoundException){
                        FileNotFoundException.printStackTrace();
                    }
                }
            }
        }
    }
    public void refreshLayoutManager(){
        if(showNotesModel.equals("列表模式")){
            LinearLayoutManager linerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linerLayoutManager);
        }else if(showNotesModel.equals("宫格模式")){
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);  //两列，纵向排列
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
    }
    @SuppressLint("RestrictedApi")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fab.setVisibility(View.VISIBLE);
        if (id == R.id.nav_all) {
            // Handle the camera action
            groupName = "全部";
        } else if (id == R.id.nav_unGrouped) {
            groupName = "未分组";
        } else if (id == R.id.nav_life) {
            groupName = "生活";
        } else if (id == R.id.nav_work) {
            groupName = "工作";
        } else if (id == R.id.nav_recycle) {
            groupName = "回收站";
            fab.setVisibility(View.INVISIBLE);
        }else if (id == R.id.nav_share) {
//            groupName = "分享软件";
            //由于qq，微信需要注册，所以暂时没弄 只能分享到系统自带的应用中
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "一款界面唯美的简易便签APP\n\n" +
                    "请复制到浏览器下载\n\n" +
                    "https://xioabaibuai.lanzous.com/b00zen0sb");
            startActivity(intent.createChooser(intent,"分享到"));
        } else if (id == R.id.nav_aboutWriter) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog alertDialog = builder.setTitle("关于作者：")
                    .setMessage("华东交通大学： " +
                            "肖定峰\n\n已开源，Github地址：https://github.com/xiaodingfeng/YYNote.git").create();
            alertDialog.show();
        }
        else if(id == R.id.nav_aboutApp){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog alertDialog = builder.setTitle("帮助文档")
                    .setMessage("书写便签时，直接返回也可保存便签\n\n" +
                            "非回收站便签长按可删除，加入回收站\n\n" +
                            "回收站便签长按可恢复，也可永久删除\n\n" +
                            "便签内页可设置提醒功能，取消提醒，设置文字大小，移动分组\n\n" +
                            "主界面菜单设置每日一文，标题栏左滑或者右滑随机一篇美文\n\n" +
                            "正文长按设置字体大小\n\n" +
                            "左侧分组目录头添加天气，需要移动网络才能准确定位，一分钟更新\n\n" +
                            "菜单设置主页面背景").create();
            alertDialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        changedGroup();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes();
        refreshAdapter();
    }

    private void refreshAdapter(){
        //这里应该是 输入一个组名 刷新这个组名下的 notes
        //刷新 adapter中的数据
        noteAdapter.setNotes(notes);
    }

    private void changedGroup(){
        //select 语句
        //刷新数据
        refreshNotes();
        refreshAdapter();
    }

    private LinkedList<Note> filter(LinkedList<Note> noteList, String text){
        LinkedList<Note> filterString = new LinkedList<>();
        for(Note note:noteList){
            if(note.getContent().contains(text)){
                filterString.add(note);
            }
        }
        return filterString;
    }

}
