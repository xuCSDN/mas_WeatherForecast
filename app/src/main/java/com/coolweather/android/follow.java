package com.coolweather.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.coolweather.android.db.City;

import org.litepal.crud.DataSupport;
import org.litepal.util.Const;

import java.util.ArrayList;
import java.util.List;

public class follow extends AppCompatActivity {

    private ListView listView;
    Intent intent;
    List<City> list=new ArrayList<>();
    List<String> list2=new ArrayList<>();//用来传给adapter
    private ArrayAdapter<String> adapter;
    private Button follow_back;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow);
        listView=(ListView)findViewById(R.id.follow_listview);
        follow_back=(Button)findViewById(R.id.follow_back);

        //接收关注城市并存储数据库中
        intent=getIntent();
        if(intent.getStringExtra("cityid").equals("1")){//通过1来判断是从搜索城市页面跳转过来的
            list.clear();
            list2.clear();
            list= DataSupport.findAll(City.class);//无需向数据库中添加城市，直接查找数据库显示即可
            for(int i=0;i<list.size();i++){
                String str=list.get(i).getCityName();
                list2.add(str);
            }
            adapter = new ArrayAdapter<String>(follow.this, android.R.layout.simple_list_item_1,list2);
            listView.setAdapter(adapter);
        }
        else{                                   //从天气页面跳转过来，需要增加该城市信息
            City city=new City();
            city.setCityName(intent.getStringExtra("cityname"));
            city.setcityid(intent.getStringExtra("cityid"));
            List<City> st=DataSupport.select("cityname").where("cityname=?", city.getCityName()).find(City.class);
            if(st.size()==0){               //通过城市名查找数据库中是否已经存在该城市，没有就插入数据库，有就不插入
                city.save();
            }

            list.clear();
            list2.clear();
            list= DataSupport.findAll(City.class);
            for(int i=0;i<list.size();i++){
                String str=list.get(i).getCityName();
                list2.add(str);
            }
            adapter = new ArrayAdapter<String>(follow.this, android.R.layout.simple_list_item_1,list2);
            listView.setAdapter(adapter);
        }

        //返回主页面按钮
        follow_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent(follow.this,MainActivity.class);
                startActivity(intent2);
                finish();
            }
        });

        //listview监听
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(follow.this, WeatherActivity.class);
                intent.putExtra("cityid",list.get(i).getcityid());
                intent.putExtra("cityname",list.get(i).getCityName());
                startActivity(intent);
                finish();
            }
        });

        //长按删除关注城市
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DataSupport.deleteAll(City.class,"cityname=?",list.get(position).getCityName());//从数据库中删除该城市
                list.clear();
                list2.clear();
                list= DataSupport.findAll(City.class);//重新从数据库中读取所有关注城市显示
                for(int i=0;i<list.size();i++){
                    String str=list.get(i).getCityName();
                    list2.add(str);
                }
                adapter = new ArrayAdapter<String>(follow.this, android.R.layout.simple_list_item_1,list2);
                listView.setAdapter(adapter);
                return true;
            }
        });

    }

}
