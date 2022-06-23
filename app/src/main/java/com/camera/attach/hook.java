package com.camera.attach;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class hook implements IXposedHookLoadPackage {
    private Class decrypt = null;
    private Class BaseActivity = null;
    private Object BaseActivityObj = null;
    private Class FragmentActivity = null;
    private String imgurl = "";
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("imgurl")) {
                imgurl = intent.getStringExtra("url");
            }
        }
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        String packageName = lpp.packageName;
        XposedBridge.log("camera load->" + packageName);
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("camera attach->" + packageName);

                Context context = (Context) param.args[0];
                Application application = (Application) param.thisObject;
                ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                if (decrypt != null) {
                    return;
                }
                decrypt = cl.loadClass("com.aerozhonghuan.serverstation.modules.repair.RepairFragment$MySelectCallback");
                if (BaseActivity == null) {
                    FragmentActivity = cl.loadClass("androidx.fragment.app.FragmentActivity");
                    XposedBridge.log("=========com.camera.attach load->" + packageName);
                }
//                XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        Activity thisObject = (Activity) param.thisObject;
//                        if (thisObject.getClass().getName().contains("MainActivity")) {
//                            XposedBridge.log("!!!!!!!!!!!!!!com.camera.attach activity->" + thisObject.getClass().getName());
//                            BaseActivity = cl.loadClass(thisObject.getClass().getName());
//                            BaseActivityObj = thisObject;
//                            XposedHelpers.findAndHookMethod(BaseActivity, "onActivityResult", int.class, int.class, Intent.class, new XC_MethodHook() {
//                                @Override
//                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                                    Toast.makeText(context, "图片获取中", Toast.LENGTH_SHORT).show();
//                                    XposedBridge.log("------------------------camera choose->" + packageName);
//                                    int requestCode = (int) param.args[0];
//                                    Intent data = (Intent) param.args[2];
//                                    switch (requestCode) {
//                                        case 2:
//                                            try {
//                                                Uri uri = data.getData();
//                                                String filePath = FileUtil.getFilePathByUri(context, uri);
//                                                if (!TextUtils.isEmpty(filePath)) {
//                                                    imgurl = filePath;
//                                                    Toast.makeText(context, "图片切换完成,请点击拍照上传" + filePath, Toast.LENGTH_SHORT).show();
//                                                }
//                                            } catch (Exception e) {
//                                                imgurl = "";
//                                            }
//                                            break;
//                                    }
//                                    super.beforeHookedMethod(param);
//                                }
//                            });
//                        }
//                        super.afterHookedMethod(param);
//                    }
//                });

                Class FileUtils = cl.loadClass("com.aerozhonghuan.serverstation.utils.FileUtils");
//                Class SelectImageDialog = cl.loadClass("com.aerozhonghuan.serverstation.widget.SelectImageDialog");
                Class SelectImageDialog = cl.loadClass("com.aerozhonghuan.serverstation.modules.repair.RepairFragment");
                XposedBridge.log("camera load->" + packageName);
                XposedHelpers.findAndHookMethod(decrypt, "onTakePicture", File.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (imgurl != null && imgurl.length() > 0) {
                            param.args[0] = new File(imgurl);//"/sdcard/DCIM/Camera1/1000.jpg"
                        }
                        super.beforeHookedMethod(param);
                    }
                });
                XposedBridge.log("camera load2->" + packageName);
                XposedHelpers.findAndHookMethod(FileUtils, "deleteSingleFile", String.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        return null;
                    }
                });
                XposedBridge.log("camera load3->" + packageName);
                XposedHelpers.findAndHookMethod(SelectImageDialog, "startTakePhoto", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Toast.makeText(context, "拍照拦截中,如有需要请选择图片再点拍照", Toast.LENGTH_SHORT).show();
//                        Intent mIntent = new Intent("choosePhoto");    //添加广播过滤标识
//                        mIntent.putExtra("UUID", UUID.randomUUID().toString());
//                        context.sendBroadcast(mIntent);
//                        XposedBridge.log("------------------------camera mIntent->" + packageName);
//                        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
//                        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                        XposedHelpers.callMethod(BaseActivityObj, "startActivityForResult", intentToPickPic, 2);
                        super.beforeHookedMethod(param);
                    }
                });
                IntentFilter filter = new IntentFilter("action_task");
                filter.addAction("imgurl");
                application.registerReceiver(receiver, filter);
            }
        });
    }


}
