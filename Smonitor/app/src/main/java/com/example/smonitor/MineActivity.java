package com.example.smonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MineActivity extends Fragment {

    private Activity mActivity;
    private View rootView;
    private TextView nicknameView;
    private TextView HeightView;
    private TextView SexView;
    private TextView AgeView;
    private TextView WeightView;
    private Button btn_update;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取依赖的activity对象
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mine, null);
        return rootView;
    }


    public void showInf()
    {
        File InfDirectory=new File(Environment.getExternalStorageDirectory()+"/smonitor/");
        if(!InfDirectory.exists())
        {
            InfDirectory.mkdir();
        }
        File PersonInf=new File(InfDirectory,"personInf.csv");
        if(!PersonInf.exists())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage("请先填写个人信息！");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(mActivity, AddInformation.class);
                    startActivity(intent);
                }
            });
            builder.show();
        }
        else
        {
            FileInputStream fread= null;
            try {
                fread = new FileInputStream(PersonInf);
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
                    nicknameView.setText(infList[0]);
                    HeightView.setText(infList[1]);
                    SexView.setText(infList[2]);
                    AgeView.setText(infList[3]);
                    WeightView.setText(infList[4]);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //fragment页面数据初始化
        nicknameView = rootView.findViewById(R.id.nickname_text);
        HeightView = rootView.findViewById(R.id.body_height_text);
        SexView = rootView.findViewById(R.id.sex_text);
        AgeView = rootView.findViewById(R.id.age_text);
        WeightView = rootView.findViewById(R.id.weight_text);
        btn_update = rootView.findViewById(R.id.btUpdate);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, AddInformation.class);
                startActivity(intent);
            }
        });
        showInf();
    }

    protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showInf();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("updateInf");
        mActivity.registerReceiver(this.broadcastReceiver, filter);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mActivity.unregisterReceiver(this.broadcastReceiver);
    }

}
