package com.example.smonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SportRecordActivity extends Fragment {
    private Activity mActivity;
    private View rootView;
    private ViewGroup scrollView;
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
        rootView = inflater.inflate(R.layout.fragment_sportrecords,null);
        return rootView;
    }

    private LinearLayout LinearData;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        //fragment页面数据初始化
        LinearData=mActivity.findViewById(R.id.sportsrecord);
        initFragment();
        for(int i=0;i<LinearData.getChildCount();i+=2)
        {
            View view=LinearData.getChildAt(i);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv=(TextView)v;
                    Intent intent=new Intent(mActivity,SportRecord.class);
                    intent.putExtra("time",tv.getText());
                    startActivity(intent);
                }
            });
        }
    }

    protected void initFragment()
    {
        File RecordFile=new File(Environment.getExternalStorageDirectory()+"/smonitor/SportsRecord.csv");
        if(RecordFile.exists())
        {
            FileInputStream fread=null;
            try {
                fread=new FileInputStream(RecordFile);
                int len=fread.available();
                byte[] temp = new byte[len];
                String inf = null;
                if ((len = fread.read(temp)) > 0) {
                    inf = new String(temp, 0, len);
                }
                fread.close();
                String[] tempory = inf.split("\n");
                for(int i=1;i<tempory.length;i++)
                {
                    /*
                    <TextView
                android:id="@+id/sp1"
                android:layout_width="match_parent"
                android:layout_height="50dp"
                android:text="2019/09/12"
                android:textSize="25sp"
                android:textColor="@color/text_color_white"
                android:textAlignment="center"/>
            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:background="@color/green2"/>
                     */
                    String []infList=tempory[i].split(",");
                    TextView textView=new TextView(mActivity);
                    textView.setText(infList[0]);
                    textView.setTextColor(mActivity.getResources().getColor(R.color.text_color_white));
                    textView.setTextSize(25);
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,150);
                    textView.setGravity(Gravity.CENTER);
                    textView.setLayoutParams(lp);
                    LinearData.addView(textView);
                    View Line=new View(mActivity);
                    Line.setBackgroundColor(mActivity.getResources().getColor(R.color.green2));
                    LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
                    Line.setLayoutParams(lp1);
                    LinearData.addView(Line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
