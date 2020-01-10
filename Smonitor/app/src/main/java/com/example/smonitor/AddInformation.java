package com.example.smonitor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddInformation extends AppCompatActivity implements View.OnClickListener{
    //昵称
    private EditText nickname;
    //身高
    private EditText heightOfBody;
    //体重
    private EditText weightOfBody;
    //年龄
    private EditText age;
    //性别
    private RadioGroup sexGroup;
    //进入软件的按钮
    private Button btn_enter;
    private Context mContext;

    private File Personfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        mContext=getApplicationContext();
        bindView();
        initInf();
    }
    protected void bindView()
    {
        nickname=findViewById(R.id.nickname_text);
        heightOfBody=findViewById(R.id.body_height_text);
        weightOfBody=findViewById(R.id.weight_text);
        sexGroup=findViewById(R.id.SexRadioGroup);
        age=findViewById(R.id.age_text);
        btn_enter=findViewById(R.id.btEnter);
        btn_enter.setOnClickListener(this);
    }

    public void initInf()
    {
        File InfDirectory=new File(Environment.getExternalStorageDirectory()+"/smonitor/");
        Personfile=new File(InfDirectory,"personInf.csv");
        if(!Personfile.exists())
        {
            try {
                Personfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        else {
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
                    nickname.setText(infList[0]);
                    heightOfBody.setText(infList[1]);
                    age.setText(infList[3]);
                    weightOfBody.setText(infList[4]);
                    if (infList[2].equals("男")) {
                        sexGroup.getChildAt(0).setSelected(true);
                    } else {
                        sexGroup.getChildAt(1).setSelected(true);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btEnter:
                saveInfor();
                break;
        }
    }
    public void saveInfor()
    {
        String nicknameText=nickname.getText().toString();
        String heightText=heightOfBody.getText().toString();
        String sexText="";
        for(int i=0;i<sexGroup.getChildCount();i++)
        {
            RadioButton rb=(RadioButton) sexGroup.getChildAt(i);
            if(rb.isChecked())
            {
                sexText=rb.getText().toString();
                break;
            }
        }
        String ageText=age.getText().toString();
        String weightText=weightOfBody.getText().toString();
        if(nicknameText.equals("")||heightText.equals("")||weightText.equals("")||ageText.equals("")||sexText.equals(""))
        {
            Toast.makeText(mContext, "个人信息不能为空！", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String inf="nickname,height,sex,age,weight";
            StringBuilder sb=new StringBuilder();
            sb.append(inf+"\n");
            sb.append(nicknameText+',');
            sb.append(heightText+',');
            sb.append(sexText+',');
            sb.append(ageText+',');
            sb.append(weightText);
            try {
                FileOutputStream fwrite=new FileOutputStream(Personfile);
                fwrite.write(sb.toString().getBytes());
                fwrite.flush();
                fwrite.close();
                Intent intent=new Intent();
                intent.setAction("updateInf");
                this.sendBroadcast(intent);
                this.finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
