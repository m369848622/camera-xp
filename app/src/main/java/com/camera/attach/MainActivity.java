package com.camera.attach;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static String ThisUUID = "";
    private static boolean isrun = false;
    String[] mPermissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        //new一个广播接收器，注册广播需要，以及在此处加入收到广播后的逻辑代码
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("actionactionaction", action);
            if (action.equals("choosePhoto")) {//根据过滤标识区分广播
                String temp = intent.getStringExtra("UUID");
                if (!ThisUUID.equals(temp)) {
                    Log.i("choosePhoto", action);
                    ThisUUID = temp;
                    choosePhoto();
                }
            }
        }
    };

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button loginButton = (Button) findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto();
            }
        });
        Button clean = (Button) findViewById(R.id.clean);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageDrawable(null);
                Intent mIntent = new Intent("pushimgurl");    //添加广播过滤标识
                mIntent.setAction("imgurl");
                mIntent.putExtra("url", "");
                sendBroadcast(mIntent);
            }
        });
        ActivityCompat.requestPermissions(MainActivity.this, mPermissionList, 100);
        Toast.makeText(MainActivity.this, toastMessage(), Toast.LENGTH_SHORT).show();
//        IntentFilter filter = new IntentFilter("action_task");
//        filter.addAction("choosePhoto");
//        this.registerReceiver(mBroadcastReceiver, filter);
    }

    public void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                try {
                    Uri uri = data.getData();
                    String filePath = FileUtil.getFilePathByUri(this, uri);
                    if (!TextUtils.isEmpty(filePath)) {
                        Toast.makeText(MainActivity.this, "图片切换完成,请点击拍照上传" + filePath, Toast.LENGTH_SHORT).show();
                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        imageView.setImageURI(uri);
                        Intent mIntent = new Intent("pushimgurl");    //添加广播过滤标识
                        mIntent.setAction("imgurl");
                        mIntent.putExtra("url", filePath);
                        sendBroadcast(mIntent);
                    }
                } catch (Exception e) {

                }
                break;
        }
    }

    public String toastMessage() {
        return "hooking";
    }
}
