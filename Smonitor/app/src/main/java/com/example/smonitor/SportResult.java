package com.example.smonitor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SportResult extends AppCompatActivity implements View.OnClickListener{
    private RelativeLayout back;
    private MapView mapView;
    private TextView StepDisplay;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private TextView Caroli;
    private TextView AverageSpeed;
    private TextView HighSpeed;
    private TextView LowSpeed;
    private TextView Duration;
    private TextView Miles;
    private TextView Result;
    private LinearLayout shareView;

    private List<LatLng> Pathpoints=new ArrayList<>();
    private AMap aMap;
    private List<LatLng> mOriginLatLngList;
    private Polyline mOriginPolyline;
    private PathSmoothTool mpathSmoothTool = null;
    private PolylineOptions polylineOptions;

    private Marker mOriginStartMarker, mOriginEndMarker;
    private long seconds;
    private float miles;
    private float averageSpeed;
    private String startTime;
    private float Caruli;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultofsport);
        mapView=findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        bindView();
        initPolyline();
        if (aMap == null)
            aMap = mapView.getMap();
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                readData();
                evaluate();
                readPointLatLgt();
                initMapView();
            }
        });
        setUpMap();
    }

    private void initMapView()
    {
        LatLng startLatLng = Pathpoints.get(0);
        LatLng endLatLng = Pathpoints.get(Pathpoints.size()-1);
        if (Pathpoints == null || startLatLng == null || endLatLng == null) {
            return;
        }
        mOriginLatLngList = mpathSmoothTool.pathOptimize(Pathpoints);
        addOriginTrace(startLatLng, endLatLng, mOriginLatLngList);
    }
    private void initPolyline() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.text_color_blue));
        polylineOptions.width(20f);

        mpathSmoothTool = new PathSmoothTool();
        mpathSmoothTool.setIntensity(4);
    }

    private void readPointLatLgt()
    {
        String []temp=startTime.split(" ");
        String filename=temp[0]+temp[1]+".csv";
        File file=new File(Environment.getExternalStorageDirectory()+"/smonitor/"+filename);
        if(!file.exists())
        {
            return;
        }
        try {
            FileInputStream fread=new FileInputStream(file);
            int len=fread.available();
            byte[] temp1 = new byte[len];
            String inf = null;
            if ((len = fread.read(temp1)) > 0) {
                inf = new String(temp1, 0, len);
            }
            fread.close();
            String[] infList=inf.split("\n");
            for(int i=0;i<infList.length;i++)
            {
                String []latlgts=infList[i].split(",");
                LatLng l=new LatLng(Double.parseDouble(latlgts[0]),Double.parseDouble(latlgts[1]));
                Pathpoints.add(l);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DecimalFormat decimalFormat=new DecimalFormat("0.00");
    protected void readData()
    {
        File InfDirectory=new File(Environment.getExternalStorageDirectory()+"/smonitor/");
        File RecordFile=new File(InfDirectory,"SportsRecord.csv");
        FileInputStream fread=null;
        try {
            fread = new FileInputStream(RecordFile);
            int len = fread.available();
            byte[] temp = new byte[len];
            String inf = null;
            if ((len = fread.read(temp)) > 0) {
                inf = new String(temp, 0, len);
            }
            fread.close();
            String[] tempory = inf.split("\n");
            if(tempory.length>1)
            {
                //StartTime,EndTime,Miles,TimeDuration,AverageSpeed,HighSpeed,LowSpeed,Step,Caroies
                String []LastInf=tempory[tempory.length-1].split(",");
                startTime=LastInf[0];
                StepDisplay.setText(LastInf[7]);
                Caroli.setText(LastInf[8]);
                Caruli=Float.parseFloat(LastInf[8]);
                AverageSpeed.setText(LastInf[4]);
                averageSpeed=Float.parseFloat(LastInf[4]);
                HighSpeed.setText(String.valueOf(LastInf[5]));
                LowSpeed.setText(String.valueOf(LastInf[6]));
                Duration.setText(LastInf[3]);
                String []datesSplit=LastInf[3].split(":");
                seconds=Integer.parseInt(datesSplit[0])*3600+Integer.parseInt(datesSplit[1])*60+
                        Integer.parseInt(datesSplit[2]);
                Miles.setText(LastInf[2]);
                miles=Float.parseFloat(LastInf[2]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void evaluate()
    {
        //评分规则：依次判断 距离大于0 ★；运动时间大于40分钟 ★★；速度小于等于5分/公里 ★★★
        if(averageSpeed<=5&&seconds>=40*60)
        {
            star1.setImageResource(R.mipmap.small_star);
            star2.setImageResource(R.mipmap.big_star);
            star3.setImageResource(R.mipmap.small_star);
            Result.setText("跑步效果完美");
        }
        else if(seconds>=40*60)
        {
            star1.setImageResource(R.mipmap.small_star);
            star2.setImageResource(R.mipmap.big_star);
            Result.setText("跑步效果不错");
        }
        else
        {
            star1.setImageResource(R.mipmap.small_star);
            Result.setText("跑步效果一般");
        }
    }
    protected void bindView()
    {
        back=findViewById(R.id.re_back);
        back.setOnClickListener(this);
        shareView=findViewById(R.id.ll_share);
        shareView.setOnClickListener(this);
        star1=findViewById(R.id.ivStar1);
        star2=findViewById(R.id.ivStar2);
        star3=findViewById(R.id.ivStar3);
        StepDisplay=findViewById(R.id.stepcount);
        Caroli=findViewById(R.id.tvCalorie);
        AverageSpeed=findViewById(R.id.averageSpeed);
        HighSpeed=findViewById(R.id.HighestSpeed);
        LowSpeed=findViewById(R.id.lowestSpeed);
        Duration=findViewById(R.id.tvDuration);
        Miles=findViewById(R.id.tvDistancet);
        Result=findViewById(R.id.tvResult);
    }

    //设置Amap属性
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.mylocation_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);// 设置比例尺显示
        aMap.getUiSettings().setZoomControlsEnabled(false);// 设置默认缩放按钮是否显示
        aMap.getUiSettings().setCompassEnabled(false);// 设置默认指南针是否显示
        aMap.setMyLocationEnabled(false);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

  //添加轨迹
    private void addOriginTrace(LatLng startPoint, LatLng endPoint,
                                List<LatLng> originList) {
        polylineOptions.addAll(originList);
        mOriginPolyline = aMap.addPolyline(polylineOptions);
        mOriginStartMarker = aMap.addMarker(new MarkerOptions().position(
                startPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.sport_start)));
        mOriginEndMarker = aMap.addMarker(new MarkerOptions().position(
                endPoint).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.sport_end)));

        try {
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(), 16));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LatLngBounds getBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (mOriginLatLngList == null) {
            return b.build();
        }
        for (LatLng latLng : mOriginLatLngList) {
            b.include(latLng);
        }
        return b.build();
    }

    //分享文本
    private void systemShareText() {
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("text/plain");//设置分享内容的类型
        share_intent.putExtra(Intent.EXTRA_SUBJECT, "今日运动");//添加分享内容标题
        share_intent.putExtra(Intent.EXTRA_TEXT,"今日运动"+String.valueOf(miles)+
                "公里,消耗能量"+String.valueOf(Caruli)+"卡!");//添加分享内容
        share_intent = Intent.createChooser(share_intent, "分享");
        startActivity(share_intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.re_back:
                /*
                Intent intent=new Intent();
                intent.setAction("retBack");
                this.sendBroadcast(intent);*/
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.ll_share:
                systemShareText();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // 表示按返回键 时的操作
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
