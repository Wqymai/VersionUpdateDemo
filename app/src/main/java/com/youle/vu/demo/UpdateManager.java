package com.youle.vu.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by wuqiyan on 16/5/6.
 */
public  class UpdateManager {

    private Context mContext;

    //提示语
    private String updateMsg = "有最新的软件包哦！";

    // 服务器上安装包url
    private String apkUrl = "http://192.168.199.166/demo.apk";

    private Dialog noticeDialog;
    private Dialog downloadDialog;

    private static final String savePath = "/sdcard/updatedemo/";
    private static final String saveFileName = savePath + "UpdateDemoRelease.apk";

    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private int progress;
    private Thread downLoadThread;
    private boolean interceptFlag = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    downloadDialog.dismiss();
                    break;
                default:
                    break;

            }

        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    //外部接口让主Activity调用
    public void checkUpdateInfo() {
        showNoticeDialog();
    }

    private void showNoticeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();

    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("正在下载更新包");
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(com.youle.vu.demo.R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(com.youle.vu.demo.R.id.progress);
        builder.setView(v);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();
        downloadApk();
    }

    private void downloadApk() {
        downLoadThread=new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                Log.i("%%%%%%%%", "len:" + length);
                InputStream is = conn.getInputStream();
                //                File file = new File(savePath);
                //                if (!file.exists()) {
                //                    file.mkdir();
                //                }
                //                String apkFile = saveFileName;
                //                File ApkFile = new File(apkFile);
                //                FileOutputStream fos = new FileOutputStream(ApkFile);
                File file = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk");

                FileOutputStream fos = new FileOutputStream(file);

                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numread = is.read(buf);
                    Log.i("&&&&&&&&&&","numread:"+numread);
                    count += numread;
                    progress =(int)(((float)count / length) * 100);
                    Log.i("&&&&&&&&&&","progress:"+progress);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);


                } while (!interceptFlag);//点击取消就停止下载
                fos.close();
                is.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };
}
