package com.example.smonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView sportsStart;
    private ImageView sportsRecords;
    private ImageView mine;
    private RelativeLayout fragmentLayout;
    private MineActivity mineActivity;
    private SportRecordActivity sportRecordActivity;
    private SportStartActivity sportStartActivity;
    private Fragment mContent;
    private String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.ACCESS_FINE_LOCATION};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        bindView();
        initFragment();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(permission,
                    1);
        }
    }
    /*
    public void createFile()
    {
        try {
            FileOutputStream fwrite=openFileOutput("personInf.txt", Context.MODE_PRIVATE);
            fwrite.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    public void bindView()
    {
        sportsStart=findViewById(R.id.sports);
        sportsRecords=findViewById(R.id.sports_memory);
        mine=findViewById(R.id.person);
        fragmentLayout=findViewById(R.id.fraglayout);

        sportsRecords.setOnClickListener(this);
        sportsStart.setOnClickListener(this);
        mine.setOnClickListener(this);

    }

    public void initFragment()
    {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        if(mineActivity!=null&&mineActivity.isAdded())
        {
            transaction.remove(mineActivity);
        }
        if(sportStartActivity!=null&&sportStartActivity.isAdded())
        {
            transaction.remove(sportStartActivity);
        }
        if(sportRecordActivity!=null&&sportRecordActivity.isAdded())
        {
            transaction.remove(sportRecordActivity);
        }
        transaction.commitAllowingStateLoss();
        mineActivity=null;
        sportStartActivity=null;
        sportRecordActivity=null;
        sportsStart.performClick();
    }

    public void switchContent(View view)
    {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        Fragment fragment=sportStartActivity;
        if(view==mine)
        {
            if(mineActivity==null)
            {
                mineActivity=new MineActivity();
            }
            fragment=mineActivity;
        }
        else if(view==sportsStart)
        {
            if(sportStartActivity==null)
            {
                sportStartActivity=new SportStartActivity();
            }
            fragment=sportStartActivity;
        }
        else if(view==sportsRecords)
        {
            if(sportRecordActivity==null)
            {
                sportRecordActivity=new SportRecordActivity();
            }
            fragment=sportRecordActivity;
        }
        if(mContent==null)
        {
            transaction.add(fragmentLayout.getId(),fragment).commit();
            mContent=fragment;
        }
        if(mContent!=fragment)
        {
            if(!fragment.isAdded())
            {
                transaction.hide(mContent).add(fragmentLayout.getId(),fragment).commitNowAllowingStateLoss();

            }
            else
            {
                transaction.hide(mContent).show(fragment).commitNowAllowingStateLoss();
            }
            mContent=fragment;
        }
        sportsStart.setSelected(false);
        sportsRecords.setSelected(false);
        mine.setSelected(false);
        view.setSelected(true);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.person:
                switchContent(mine);
                break;
            case R.id.sports_memory:
                switchContent(sportsRecords);
                break;
            case R.id.sports:
                switchContent(sportsStart);
                break;
        }
    }
}
