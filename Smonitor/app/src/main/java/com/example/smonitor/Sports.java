package com.example.smonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Sports extends AppCompatActivity implements View.OnClickListener{

    private RelativeLayout sportContent;
    private MapView mapView;
    private Chronometer displayTime;
    private TextView miles;
    private TextView speeed;
    private TextView stepCount;
    private Button btn_finish;
    private Button btn_continue;
    private Button btn_pause;
    private SensorManager mSensorManager;
    private Sensor stepDector;
    private SensorEventListener stepDectorListener;
    //地图定位相关
    private AMap aMap;
    private LocationSource.OnLocationChangedListener mListener=null;
    private AMapLocationClient mLocationClient=null;
    private AMapLocationClientOption mLocationOption;
    private final long LocationInterval=2000;


    private Polyline polyline;
    private PolylineOptions polylineOptions;
    private List<LatLng> SportsLatLng=new ArrayList<>();
    private PathSmoothTool pathSmoothTool=null;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private long startTime;
    private long endTime;
    private double distance=0;//里程
    private long seconds=0;
    private List<LatLng> mPathLinePoints = new ArrayList<>();//记录所有点
    private double highSpeed=1000;
    private double lowSpeed=0;
    private double averageSpeed=0;
    private class MyRunnable implements Runnable{
        @Override
        public void run() {
            displayTime.setText(formatseconds());
            mHandler.postDelayed(this,1000);
        }
    }

    private int steps=0;//步数
    private MyRunnable runnable=null;
    public String formatseconds() {
        String hh = seconds / 3600 > 9 ? seconds / 3600 + "" : "0" + seconds
                / 3600;
        String mm = (seconds % 3600) / 60 > 9 ? (seconds % 3600) / 60 + ""
                : "0" + (seconds % 3600) / 60;
        String ss = (seconds % 3600) % 60 > 9 ? (seconds % 3600) % 60 + ""
                : "0" + (seconds % 3600) % 60;

        seconds++;
        return hh + ":" + mm + ":" + ss;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sportsmap);
        mapView=findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        bindView();
        startTime=System.currentTimeMillis();
        runnable=new MyRunnable();
        mHandler.postDelayed(runnable,0);
        mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        stepDector=mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepDectorListener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                steps++;
                stepCount.setText(String.valueOf(steps));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        registerStepSensor();
        //屏幕常亮
        if(mapView!=null)
            sportContent.setKeepScreenOn(true);
        startLocation();
        if(aMap==null)
        {
            aMap=mapView.getMap();
            setMap();
        }
        initPolyLine();
    }

    protected void initPolyLine()
    {
        polylineOptions=new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.text_color_blue));
        polylineOptions.width(20f);
        pathSmoothTool=new PathSmoothTool();
        pathSmoothTool.setIntensity(4);
    }
    private LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            mListener = onLocationChangedListener;
            startLocation();
        }

        @Override
        public void deactivate() {
            mListener = null;
            if (mLocationClient != null) {
                mLocationClient.stopLocation();
                mLocationClient.onDestroy();
            }
            mLocationClient = null;
        }
    };
    private void setMap() {
        aMap.setLocationSource(locationSource);// 设置定位监听
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.mylocation_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        // 设置定位的类型为定位模式 ，定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
//        myLocationStyle.interval(interval);//设置发起定位请求的时间间隔
//        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，true 显示，false不显示
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);// 设置默认缩放按钮是否显示
        aMap.getUiSettings().setCompassEnabled(false);// 设置默认指南针是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }
    protected void startLocation()
    {
        if(mLocationClient==null)
        {
            mLocationClient=new AMapLocationClient(this);
            //设置定位的属性
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(LocationInterval);
            mLocationOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
            mLocationClient.setLocationOption(mLocationOption);

            // 设置定位监听
            mLocationClient.setLocationListener(aMapLocationListener);
            //开始定位
            mLocationClient.startLocation();
        }
    }

    private AMapLocationListener aMapLocationListener=new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation==null)
                return;
            if(aMapLocation.getErrorCode()==0)
            {
                updateLocation(aMapLocation);
            }
            else
            {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    };

    private DecimalFormat decimalFormat=new DecimalFormat("0.00");
    protected void updateLocation(AMapLocation location)
    {
        mPathLinePoints.add(new LatLng(location.getLatitude(),location.getLongitude()));
        int len=mPathLinePoints.size();
        double nearDis=AMapUtils.calculateLineDistance(mPathLinePoints.get(len-2),mPathLinePoints.get(len-1));
        distance+=nearDis;
        //计算速度
        double sportMiles=distance/1000d;
        nearDis=nearDis/1000d;
        if(seconds>0&&sportMiles>0.05)
        {
            averageSpeed=(double)seconds/60f/sportMiles;
            double momSpeed=(double)2f/60f/nearDis;
            if(highSpeed>momSpeed)
                highSpeed=momSpeed;
            if(lowSpeed<momSpeed)
                lowSpeed=momSpeed;
            speeed.setText(decimalFormat.format(averageSpeed));
        }
        miles.setText(decimalFormat.format(sportMiles));
        SportsLatLng.clear();
        SportsLatLng=new ArrayList<>(pathSmoothTool.pathOptimize(mPathLinePoints));
        if(!SportsLatLng.isEmpty())
        {
            polylineOptions.add(SportsLatLng.get(SportsLatLng.size()-1));
            if(mListener!=null)
            {
                mListener.onLocationChanged(location);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                        location.getLongitude()),18));
            }
        }
        polyline=aMap.addPolyline(polylineOptions);
    }
    protected void removeLocation()
    {
        if(mapView!=null)
            sportContent.setKeepScreenOn(false);
        //解除定位服务
        if(mLocationClient!=null)
        {
            mLocationClient.stopLocation();
            mLocationClient.unRegisterLocationListener(aMapLocationListener);
            mLocationClient.onDestroy();
            mLocationClient=null;
        }
    }
    protected void registerStepSensor()
    {
        if(stepDector!=null)
            mSensorManager.registerListener(stepDectorListener,stepDector,SensorManager.SENSOR_DELAY_FASTEST);
    }
    protected void unregisterStepSensor()
    {
        if(stepDector!=null)
            mSensorManager.unregisterListener(stepDectorListener);
    }
    protected void bindView()
    {
        sportContent=findViewById(R.id.sport_content);
        displayTime=findViewById(R.id.cm_passtime);
        //displayTime.setBase(SystemClock.elapsedRealtime());
        miles=findViewById(R.id.tvMileage);
        speeed=findViewById(R.id.tvSpeed);
        stepCount=findViewById(R.id.stepCount);
        btn_continue=findViewById(R.id.tv3);
        btn_finish=findViewById(R.id.tv1);
        btn_pause=findViewById(R.id.tv2);

        btn_pause.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
        //displayTime.start();
    }
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.tv1:
                if(runnable!=null)
                {
                    mHandler.removeCallbacks(runnable);
                    runnable = null;
                }
                endTime=System.currentTimeMillis();
                unregisterStepSensor();
                removeLocation();
                saveRecord();
                savePathPoint();
                Intent intent=new Intent(this,SportResult.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv2:
                if(runnable!=null)
                {
                    mHandler.removeCallbacks(runnable);
                    runnable = null;
                }
                unregisterStepSensor();
                removeLocation();
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(SportsLatLng),20));
                break;
            case R.id.tv3:
                if(runnable==null)
                    runnable=new MyRunnable();
                mHandler.postDelayed(runnable,0);
                registerStepSensor();
                //屏幕保持常亮
                if (null != mapView)
                    sportContent.setKeepScreenOn(true);
                startLocation();
                break;
        }
    }

    protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("retBack");
        this.registerReceiver(this.broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.unregisterReceiver(this.broadcastReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // 表示按返回键 时的操作
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("确定退出?");
                dialog.setMessage("退出将不会保存本次运动记录");
                dialog.setCancelable(true);    //设置是否可以通过点击对话框外区域或者返回按键关闭对话框
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    private SimpleDateFormat simpleformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    protected void saveRecord()
    {
        File InfDirectory=new File(Environment.getExternalStorageDirectory()+"/smonitor/");
        File RecordFile=new File(InfDirectory,"SportsRecord.csv");
        StringBuilder sb=new StringBuilder();
        sb.append(simpleformat.format(startTime)+",");
        sb.append(simpleformat.format(endTime)+",");
        sb.append(String.valueOf(decimalFormat.format(distance/1000f))+",");
        sb.append(displayTime.getText()+",");
        sb.append(String.valueOf(decimalFormat.format(averageSpeed))+",");
        sb.append(String.valueOf(decimalFormat.format(highSpeed))+",");
        sb.append(String.valueOf(decimalFormat.format(lowSpeed))+",");
        sb.append(stepCount.getText()+",");
        double Caloli=calculateCaroli(distance/1000d);
        sb.append(String.valueOf(decimalFormat.format(Caloli))+"\n");
        if(!RecordFile.exists())
        {
            try {
                RecordFile.createNewFile();
                FileOutputStream outputStream=new FileOutputStream(RecordFile);
                String titles="StartTime,EndTime,Miles,TimeDuration,AverageSpeed,HighSpeed,LowSpeed,Step,Caroies\n";
                outputStream.write(titles.getBytes());
                outputStream.write(sb.toString().getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                RandomAccessFile raf = new RandomAccessFile(RecordFile,"rw");
                raf.seek(RecordFile.length());
                raf.write(sb.toString().getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void savePathPoint()
    {
        String []temp=simpleformat.format(startTime).split(" ");
        String filename=temp[0]+temp[1]+".csv";
        File file=new File(Environment.getExternalStorageDirectory()+"/smonitor/"+filename);
        if(!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos=new FileOutputStream(file);
            for(int i=0;i<mPathLinePoints.size();i++)
            {
                String value=String.valueOf(mPathLinePoints.get(i).latitude)+","+String.valueOf(mPathLinePoints.get(i).longitude)+"\n";
                fos.write(value.getBytes());
            }
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private double calculateCaroli(double sportMiles)
    {
        double weight=0;
        File Personfile=new File(Environment.getExternalStorageDirectory()+"/smonitor/personInf.csv");
        FileInputStream fread= null;
        try {
            fread = new FileInputStream(Personfile);
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
                String []infList=tempory[1].split(",");
                weight=Double.parseDouble(infList[4]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weight * sportMiles * 1.036;
    }
    private LatLngBounds getBounds(List<LatLng> pointlist) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (pointlist == null) {
            return b.build();
        }
        for (LatLng latLng : pointlist) {
            b.include(latLng);
        }
        return b.build();

    }
}
