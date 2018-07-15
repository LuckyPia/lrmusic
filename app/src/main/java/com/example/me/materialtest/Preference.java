package com.example.me.materialtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preference{
    private static Preference pref = null;
    Context ctx = null;
    Preference(Context ctx){
        this.ctx = ctx;
    }
    private SharedPreferences sp;

    static Preference getInstance(Context ctx){
        if(pref == null){
            pref = new Preference(ctx);
        }
        return pref;
    }

    public void save(String type){
        sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        Editor e = sp.edit();
        e.putString("TYPE", type);
        e.apply();
    }

    public String get(){
        sp = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sp.getString("TYPE", "1");
    }
}