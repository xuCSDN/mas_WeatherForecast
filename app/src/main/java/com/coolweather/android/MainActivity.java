package com.coolweather.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.coolweather.android.gson.Data_city;
import com.coolweather.android.gson.Province;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private EditText input;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Data_city.LocationDTO> cityList=new ArrayList<>();
    private String inputcity;//接收用户输入的城市
    private Button find;
    private Button find_f;
    private Button find_follow;
    private List<Data_city.LocationDTO> list=new ArrayList<>();

    private List<Province> cityList1=new ArrayList<>();
    private List<Province> list1=new ArrayList<>();
    private List<String> datalist2=new ArrayList<>();

    private int lag=0;//用来判断是按钮点击显示listview还是点击省显示市

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find);
        listView = (ListView) findViewById(R.id.listview);
        input = (EditText) findViewById(R.id.input);
        find = (Button) findViewById(R.id.b_find);
        find_f=(Button)findViewById(R.id.b_find_f);
        find_follow=(Button)findViewById(R.id.b_follow);

        String address1 = "http://guolin.tech/api/china";
        cityList1 = queryFromServer3(address1);

        //搜索城市按钮
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputcity = input.getText().toString();
                if(!inputcity.isEmpty()){
                    String address = "https://geoapi.qweather.com/v2/city/lookup?location=" + inputcity + "&key=6c689b02455042aa99cbb5da164822a4&number=20";
                    cityList = queryFromServer(address);
                }
                else{
                    Toast.makeText(MainActivity.this,"城市名不能为空！",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //查询城市天气按钮
        find_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputcity = input.getText().toString();
                if(!inputcity.isEmpty()) {
                    String address = "https://geoapi.qweather.com/v2/city/lookup?location=" + inputcity + "&key=6c689b02455042aa99cbb5da164822a4&number=20";
                    cityList = queryFromServer2(address);
                }
                else{
                    Toast.makeText(MainActivity.this,"城市名不能为空！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //查看关注按钮
        find_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3=new Intent(MainActivity.this,follow.class);
                intent3.putExtra("cityid","1");//赋值1是为了和天气页面的关注按钮区分，都是跳转到关注页面
                startActivity(intent3);
                finish();
            }
        });

        //listview每一项可点击
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(lag==0){
                    String clickcity =datalist2.get(i);
                    String address = "https://geoapi.qweather.com/v2/city/lookup?location=" + clickcity + "&key=6c689b02455042aa99cbb5da164822a4&number=20";
                    cityList = queryFromServer(address);
                }
                else{
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    intent.putExtra("cityid",list.get(i).getId());
                    intent.putExtra("cityname",list.get(i).getName());
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    /**
     * 向服务器发出请求并处理
     */
    private List<Data_city.LocationDTO> queryFromServer(String address) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();//创建客户端对象
                Request request = new Request.Builder().url(address).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                if(responseData.length()<15){//当城市名不正确时返回的字符串长度小于15，用来给出提示：没有此城市
                    error();
                }
                else{
                    Data_city data = JSON.parseObject(responseData, Data_city.class);
                    list = data.getLocation();
                    refresh();//在该函数中执行runonUIthread
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
        return list;
    }

    /**
     * 更新UI界面，显示listview
     */
    private void refresh() {
        runOnUiThread(()->{
            lag=1;
            dataList.clear();
            for (Data_city.LocationDTO province : list) {
                dataList.add(province.getName());
            }
            adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,dataList);
            listView.setAdapter(adapter);
        });
    }

    /**
     * 没有此城市给出提示
     */
    private void error() {
        runOnUiThread(()->{
            Toast.makeText(MainActivity.this,"没有此城市！",Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * 查询功能，直接跳转天气页面
     */
    private List<Data_city.LocationDTO> queryFromServer2(String address) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();//创建客户端对象
                Request request = new Request.Builder().url(address).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                if(responseData.length()<15){//当城市名不正确时返回的字符串长度小于15，用来给出提示：没有此城市
                    error();
                }
                else {
                    Data_city data = JSON.parseObject(responseData, Data_city.class);
                    list = data.getLocation();
                    refresh2();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
        return list;
    }

    /**
     * 查询城市天气，直接跳转天气页面
     */
    private void refresh2() {
        runOnUiThread(()->{
             Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
             intent.putExtra("cityid",list.get(0).getId());
             intent.putExtra("cityname",list.get(0).getName());
             startActivity(intent);
             finish();
        });
    }

    /**
     * 向服务器发出请求并处理
     */
    private List<Province> queryFromServer3(String address) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();//创建客户端对象
                Request request = new Request.Builder().url(address).build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                if(responseData.length()<15){//当城市名不正确时返回的字符串长度小于15，用来给出提示：没有此城市
                    error();
                }
                else{
                    refresh3(responseData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
        return list1;
    }

    /**
     * 更新UI界面，显示listview
     */
    private void refresh3(String str) {
        runOnUiThread(()->{

            try{
                datalist2.clear();
                JSONArray  j = new JSONArray(str);
                for(int i=0;i<j.length();i++){
                    JSONObject jb=j.getJSONObject(i);
                    datalist2.add((String) jb.get("name"));
                }
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,datalist2);
                listView.setAdapter(adapter);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });
    }

}