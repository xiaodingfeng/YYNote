package com.example.xiaobai.yynote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.ViewDebug;

import com.example.xiaobai.yynote.adapter.NoteAdapter;
import com.example.xiaobai.yynote.bean.Note;

import java.util.LinkedList;

public class NoteDbHelpBusiness {

    private static NoteDbHelpBusiness dbBus = null;
    private NotesDatabaseHelper mHelper;
    private SQLiteDatabase db;
    private NoteAdapter adapter;
    private  NoteDbHelpBusiness(Context context){
        mHelper = NotesDatabaseHelper.getInstance(context);
    }

    public synchronized static NoteDbHelpBusiness getInstance(Context context){
        if(dbBus == null){
            dbBus = new NoteDbHelpBusiness(context);
        }
        return dbBus;
    }
    //获得所有便签 用于展示 “所有便签” 这个虚拟分组
    public LinkedList<Note> getAll(){
        LinkedList<Note> notes = new LinkedList<Note>();
        db = mHelper.getReadableDatabase();
        Cursor cursor=db.query(NotesDatabaseHelper.TABLE.NOTE,null,"groupName <>?",new String[]{"回收站"},null,null,"createTime Desc");
        while(cursor.moveToNext()){
            Note note = new Note();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subContent = cursor.getString(cursor.getColumnIndex("subContent"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String groupName = cursor.getString(cursor.getColumnIndex("groupName"));
            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

            note.setId(id); note.setContent(content); note.setGroupName(groupName); note.setCreateTime(createTime);
            note.setTitle(title);note.setSubContent(subContent);

            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    //获得一个组的所有便签
    public LinkedList<Note> getNotesByGroup(String GroupName){
        LinkedList<Note> notes = new LinkedList<Note>();
        db = mHelper.getReadableDatabase();
        Cursor cursor=db.query(NotesDatabaseHelper.TABLE.NOTE,null,"groupName = ?",new String[]{GroupName},null,null,"createTime Desc");
        while(cursor.moveToNext()){
            Note note = new Note();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subContent = cursor.getString(cursor.getColumnIndex("subContent"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String groupName = cursor.getString(cursor.getColumnIndex("groupName"));
            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

            note.setId(id); note.setContent(content); note.setGroupName(groupName); note.setCreateTime(createTime);
            note.setTitle(title);note.setSubContent(subContent);

            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    public void addNote(Note note){
        db = mHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title",note.getTitle());
        contentValues.put("subContent",note.getSubContent());
        contentValues.put("content",note.getContent());
        contentValues.put("createTime",note.getCreateTime());
        contentValues.put("groupName",note.getGroupName());

        db.insert(NotesDatabaseHelper.TABLE.NOTE,null,contentValues);
    }

    public void deleteNote(Note note){
        db = mHelper.getWritableDatabase();

        Integer id = note.getId();
        String id1 = id.toString();

        db.delete(NotesDatabaseHelper.TABLE.NOTE,"id = ?",new String[]{id1});
    }
    public void updateImage(String uri){
        ContentValues contentValues = new ContentValues();
        contentValues.put("uri",uri);
        db = mHelper.getWritableDatabase();
        db.execSQL("delete from touxiang");
        db.insert("touxiang",null,contentValues);
    }
    public String seletimage(){
        db = mHelper.getWritableDatabase();
        Cursor cursor=db.query("touxiang",null,null,null,null,null,null);
        if (cursor.getCount()>=0){
            while(cursor.moveToNext()){
                return cursor.getString(cursor.getColumnIndex("uri"));
            }
        }
        return null;
    }
    public void UpdateGroupName(Note note,String GroupName){
        db = mHelper.getWritableDatabase();
        Integer id = note.getId();
        String id1 = id.toString();

        db.execSQL("update note set groupName=? where id=?",new String[]{GroupName,id1});
    }

}
