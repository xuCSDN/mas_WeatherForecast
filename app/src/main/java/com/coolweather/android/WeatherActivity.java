package com.coolweather.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.coolweather.android.db.City;
import com.coolweather.android.db.Save_city;
import com.coolweather.android.gson.Data_weather1;
import com.coolweather.android.gson.Data_weather3;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private String mWeatherId;
    private TextView cityname;
    private TextView nowtime;
    private TextView nowtext;
    private TextView nowtemp;
    private TextView nowshidu;
    private TextView f1_date;
    private TextView f1_text;
    private TextView f1_tempmax;
    private TextView f1_tempmin;
    private TextView f2_date;
    private TextView f2_text;
    private TextView f2_tempmax;
    private TextView f2_tempmin;
    private TextView f3_date;
    private TextView f3_text;
    private TextView f3_tempmax;
    private TextView f3_tempmin;
    private Data_weather1 data1;
    private Data_weather3 data2;
    private Save_city save_city=new Save_city();

    private Button b_back;
    private Button b_refresh;
    private Button b_follow;

    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);

        cityname=(TextView) findViewById(R.id.cityname);
        nowtime=(TextView) findViewById(R.id.nowtime);
        nowtext=(TextView) findViewById(R.id.nowtext);
        nowtemp=(TextView) findViewById(R.id.nowtemp);
        nowshidu=(TextView) findViewById(R.id.nowshidu);

        f1_date=(TextView) findViewById(R.id.f1_date);
        f1_text=(TextView) findViewById(R.id.f1_text);
        f1_tempmax=(TextView) findViewById(R.id.f1_tempmax);
        f1_tempmin=(TextView) findViewById(R.id.f1_tempmin);
        f2_date=(TextView) findViewById(R.id.f2_date);
        f2_text=(TextView) findViewById(R.id.f2_text);
        f2_tempmax=(TextView) findViewById(R.id.f2_tempmax);
        f2_tempmin=(TextView) findViewById(R.id.f2_tempmin);
        f3_date=(TextView) findViewById(R.id.f3_date);
        f3_text=(TextView) findViewById(R.id.f3_text);
        f3_tempmax=(TextView) findViewById(R.id.f3_tempmax);
        f3_tempmin=(TextView) findViewById(R.id.f3_tempmin);
        b_back=(Button)findViewById(R.id.b_back);
        b_refresh=(Button)findViewById(R.id.b_refresh);
        b_follow=(Button)findViewById(R.id.b_folow);

        intent = getIntent();
        mWeatherId = intent.getStringExtra("cityid");

        //优先从数据库中读取缓存的城市天气
        List<Save_city> st=DataSupport.where("s_cityname=?",intent.getStringExtra("cityname")).find(Save_city.class);
        if(!st.isEmpty()){
            cityname.setText(intent.getStringExtra("cityname"));
            nowtime.setText("更新时间："+st.get(0).getS_updatetime());
            nowtext.setText("天气："+st.get(0).getS_text());
            nowtemp.setText("气温："+st.get(0).getS_temp()+" C");
            nowshidu.setText("湿度："+st.get(0).getS_shidu());

            f1_date.setText(st.get(0).getS_fc_date1());
            f1_text.setText(st.get(0).getS_fc_text1());
            f1_tempmax.setText(st.get(0).getS_fc_tempmax1());
            f1_tempmin.setText(st.get(0).getS_fc_tempmin1());

            f2_date.setText(st.get(0).getS_fc_date2());
            f2_text.setText(st.get(0).getS_fc_text2());
            f2_tempmax.setText(st.get(0).getS_fc_tempmax2());
            f2_tempmin.setText(st.get(0).getS_fc_tempmin2());

            f3_date.setText(st.get(0).getS_fc_date3());
            f3_text.setText(st.get(0).getS_fc_text3());
            f3_tempmax.setText(st.get(0).getS_fc_tempmax3());
            f3_tempmin.setText(st.get(0).getS_fc_tempmin3());

            System.out.println("----------数据库----------");
        }
        else {                                  //如果没有缓存就向服务器查询
            requestWeather1(mWeatherId);
            requestWeather3(mWeatherId);

            System.out.println("---------数据库中没有,查服务器-----------");
        }

        //返回到搜索城市页面
        b_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(WeatherActivity.this,MainActivity.class);
                startActivity(intent1);
                finish();
            }
        });

        //刷新按钮
        b_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {    //刷新即重新向服务器请求
                requestWeather1(mWeatherId);
                requestWeather3(mWeatherId);
                System.out.println("---------刷新查服务器-----------");
            }
        });

        //关注按钮
        b_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent4=new Intent(WeatherActivity.this,follow.class);
                intent4.putExtra("cityid",intent.getStringExtra("cityid"));
                intent4.putExtra("cityname",intent.getStringExtra("cityname"));
                startActivity(intent4);
                finish();
            }
        });
    }


    /**
     * 根据天气id请求城市实时天气信息。
     */
    public void requestWeather1(final String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/weather/now?location=" + weatherId + "&key=6c689b02455042aa99cbb5da164822a4";
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();//创建客户端对象
                Request request = new Request.Builder().url(weatherUrl).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                data1 = JSON.parseObject(responseData, Data_weather1.class);
                refresh1();             //runOnUiThread更新UI界面
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void refresh1() {
        runOnUiThread(() -> {
            cityname.setText(intent.getStringExtra("cityname"));
            nowtime.setText("更新时间："+data1.getUpdateTime());
            nowtext.setText("天气："+data1.getNow().getText());
            nowtemp.setText("气温："+data1.getNow().getTemp());
            nowshidu.setText("湿度："+data1.getNow().getHumidity());
            s(data1);       //将更新过的天气信息存入该城市类中，即更新缓存
        });
    }

    /**
     * 根据天气id请求未来三天城市天气信息。
     */
    public void requestWeather3(final String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/weather/3d?location=" + weatherId + "&key=6c689b02455042aa99cbb5da164822a4";
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();//创建客户端对象
                Request request = new Request.Builder().url(weatherUrl).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                data2 = JSON.parseObject(responseData, Data_weather3.class);
                refresh3();    //runOnUiThread更新UI界面
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        }
    private void refresh3() {
        runOnUiThread(() -> {

            f1_date.setText(data2.getDaily().get(0).getFxDate());
            f1_text.setText(data2.getDaily().get(0).getTextDay());
            f1_tempmax.setText(data2.getDaily().get(0).getTempMax());
            f1_tempmin.setText(data2.getDaily().get(0).getTempMin());

            f2_date.setText(data2.getDaily().get(1).getFxDate());
            f2_text.setText(data2.getDaily().get(1).getTextDay());
            f2_tempmax.setText(data2.getDaily().get(1).getTempMax());
            f2_tempmin.setText(data2.getDaily().get(1).getTempMin());

            f3_date.setText(data2.getDaily().get(2).getFxDate());
            f3_text.setText(data2.getDaily().get(2).getTextDay());
            f3_tempmax.setText(data2.getDaily().get(2).getTempMax());
            f3_tempmin.setText(data2.getDaily().get(2).getTempMin());

            s3(data2);          //将未来三天天气信息更新缓存
            save(save_city);   //将更新后的城市天气缓存存入数据库
        });
    }

    //点击刷新按钮后更新缓存天气
    public void save(Save_city city){
        List<Save_city> st2=DataSupport.where("s_cityname=?",intent.getStringExtra("cityname")).find(Save_city.class);
        st2.get(0).setS_updatetime(city.getS_updatetime());
        st2.get(0).setS_text(city.getS_text());
        st2.get(0).setS_temp(city.getS_temp());
        st2.get(0).setS_shidu(city.getS_shidu());

        st2.get(0).setS_fc_date1(city.getS_fc_date1());
        st2.get(0).setS_fc_tempmax1(city.getS_fc_tempmax1());
        st2.get(0).setS_fc_tempmin1(city.getS_fc_tempmin1());
        st2.get(0).setS_fc_text1(city.getS_fc_text1());

        st2.get(0).setS_fc_date2(city.getS_fc_date2());
        st2.get(0).setS_fc_tempmax2(city.getS_fc_tempmax2());
        st2.get(0).setS_fc_tempmin2(city.getS_fc_tempmin2());
        st2.get(0).setS_fc_text2(city.getS_fc_text2());

        st2.get(0).setS_fc_date3(city.getS_fc_date3());
        st2.get(0).setS_fc_tempmax3(city.getS_fc_tempmax3());
        st2.get(0).setS_fc_tempmin3(city.getS_fc_tempmin3());
        st2.get(0).setS_fc_text3(city.getS_fc_text3());

        st2.get(0).save();
    }

    /**
     * 将实时天气存入城市类中
     * @param d1
     */
    public void s(Data_weather1 d1){
        save_city.setS_cityid(intent.getStringExtra("cityid"));
        save_city.setS_cityName(intent.getStringExtra("cityname"));
        save_city.setS_updatetime(d1.getUpdateTime());
        save_city.setS_text(d1.getNow().getText());
        save_city.setS_temp(d1.getNow().getTemp());
        save_city.setS_shidu(d1.getNow().getHumidity());
    }

    /**
     * 将未来三天的天气信息存入城市类
     * @param d3
     */
    public void s3(Data_weather3 d3){
        save_city.setS_fc_date1(d3.getDaily().get(0).getFxDate());
        save_city.setS_fc_tempmax1(d3.getDaily().get(0).getTempMax());
        save_city.setS_fc_tempmin1(d3.getDaily().get(0).getTempMin());
        save_city.setS_fc_text1(d3.getDaily().get(0).getTextDay());

        save_city.setS_fc_date2(d3.getDaily().get(1).getFxDate());
        save_city.setS_fc_tempmax2(d3.getDaily().get(1).getTempMax());
        save_city.setS_fc_tempmin2(d3.getDaily().get(1).getTempMin());
        save_city.setS_fc_text2(d3.getDaily().get(1).getTextDay());

        save_city.setS_fc_date3(d3.getDaily().get(2).getFxDate());
        save_city.setS_fc_tempmax3(d3.getDaily().get(2).getTempMax());
        save_city.setS_fc_tempmin3(d3.getDaily().get(2).getTempMin());
        save_city.setS_fc_text3(d3.getDaily().get(2).getTextDay());
    }

}
