package jp.techacademy.sugaru.takano.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable{
    private String title; //タイトル
    private String contents; //内容
    private Date date; //日時

    private String category; //カテゴリ

    //↓プライマリーキーとは?
    //idをプライマリーキーとして設定
    @PrimaryKey
    private int id;
    //getter(title)
    public String getTitle(){
        return title;
    }
    //setter(title)
    public void setTitle(String title){
        this.title = title;
    }
    //getter(contents)
    public String getContents(){
        return contents;
    }
    //setter(contents)
    public void setContents(String contents){
        this.contents = contents;
    }
    //getter(date)
    public Date getDate(){
        return date;
    }
    //setter(date)
    public void setDate(Date date){
        this.date = date;
    }
    //getter(category)
    public String getCategory(){
        return category;
    }
    //setter(category)
    public void setCategory(String category){
        this.category = category;
    }
    //getter(id)
    public int getId(){
        return id;
    }
    //setter(id)
    public void setId(int id){
        this.id = id;
    }
}
