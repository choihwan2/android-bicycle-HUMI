package com.tempoup.sdj.humi;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private int id;
    private int level;
    private int exp;

    public User(){
        id = 0;
        level = 0;
        exp = 0;
    }

    // IDE에 의해 생성된 표준 게터와 세터들...

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLevel() { return level; }
    public void   setLevel(int level) { this.level = level; }
    public int    getExp() { return exp; }
    public void setExp(int exp)
    {
        this.exp = exp;
    }
    public void   addExp(int exp)
    {
        if( this.exp < 0 ) { this.exp = 0; }

        if( this.exp + exp >= 100 ){
            if( this.level >= 5 ){
                this.exp = 100;
                return;
            }else{
                level++;
                this.exp = (this.exp + exp) - 100;
                return;
            }
        }

        this.exp += exp;
    }
}