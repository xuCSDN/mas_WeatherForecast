package com.coolweather.android.db;


import org.litepal.crud.DataSupport;

public class City extends DataSupport {    //关注城市表

    private int id;

    private String cityName;

    private String cityid;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getcityid() {
        return cityid;
    }

    public void setcityid(String id) {
        this.cityid = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

}
