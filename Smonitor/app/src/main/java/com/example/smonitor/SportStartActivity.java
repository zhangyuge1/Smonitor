package com.example.smonitor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

public class SportStartActivity extends Fragment {
    private Activity mActivity;
    private View rootView;
    private Button btn_start;
    private TextView sportMiles;
    private TextView sportTime;
    private TextView sportCounts;
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //获取依赖的activity对象
        mActivity=getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_sport,null);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        //fragment页面数据初始化
        btn_start=rootView.findViewById(R.id.btStart);
        sportMiles=rootView.findViewById(R.id.tv_sport_mile);
        sportTime=rootView.findViewById(R.id.tv_sport_time);
        sportCounts=rootView.findViewById(R.id.tv_sport_count);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File InfDirectory=new File(Environment.getExternalStorageDirectory()+"/smonitor/");
                if(!InfDirectory.exists())
                {
                    InfDirectory.mkdir();
                }
                File PersonInf=new File(InfDirectory,"personInf.csv");
                if(!PersonInf.exists())
                {
                    AlertDialog.Builder builder =new AlertDialog.Builder(mActivity);
                    builder.setMessage("请先填写个人信息！");
                    builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent intent=new Intent(mActivity,AddInformation.class);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
                else
                {
                    Intent intent=new Intent(mActivity,Sports.class);
                    startActivity(intent);
                }
            }
        });
        if(mActivity.getIntent()!=null)
        {
            showInf();
        }
    }

    //显示界面的基本信息，运动时长等
    protected void showInf()
    {
        File RecordFile=new File(Environment.getExternalStorageDirectory()+"/smonitor/SportsRecord.csv");
        if(RecordFile.exists()) {
            FileInputStream fread = null;
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
                float miles=0;
                long Alltime=0;
                int number=0;
                //StartTime,EndTime,Miles,TimeDuration,AverageSpeed,HighSpeed,LowSpeed,Step,Caroies
                for (int i = 1; i < tempory.length; i++) {
                    number++;
                    String []infList=tempory[i].split(",");
                    miles+=Float.parseFloat(infList[2]);
                    String []hms=infList[3].split(":");
                    Alltime+=Integer.parseInt(hms[0])*3600+Integer.parseInt(hms[1])*60+
                            Integer.parseInt(hms[2]);
                }
                float minutes=Alltime/60f;
                DecimalFormat decimalFormat=new DecimalFormat("0.00");
                sportCounts.setText(String.valueOf(number));
                sportMiles.setText(decimalFormat.format(miles));
                sportTime.setText(decimalFormat.format(minutes));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
