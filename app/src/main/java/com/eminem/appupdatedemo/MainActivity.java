package com.eminem.appupdatedemo;

import android.Manifest;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.kcode.permissionslib.main.OnRequestPermissionsCallBack;
import com.kcode.permissionslib.main.PermissionCompat;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String url = "http://shouji.360tpcdn.com/150527/c90d7a6a8cded5b5da95ae1ee6382875/com.tencent.mm_561.apk";
    private DownloadManager downloadManager;
    public static final String DOWNLOAD_ID = "download_id";
    private long lastDownloadId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //取消下载
        findViewById(R.id.cancle_bt).setOnClickListener(this);
        //查看下载状态
        findViewById(R.id.look_bt).setOnClickListener(this);
        //开始下载
        findViewById(R.id.start_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_bt:
                downLoadApp();
                break;
            case R.id.cancle_bt:
                //取消下载， 如果一个下载被取消了，所有相关联的文件，部分下载的文件和完全下载的文件都会被删除。
                downloadManager.remove(lastDownloadId);
                break;

            case R.id.look_bt:
                Query query = new Query();
                query.setFilterById(lastDownloadId);
                Cursor cursor = downloadManager.query(query);

                if (cursor == null) {
                    Toast.makeText(MainActivity.this, "Download not found!", Toast.LENGTH_LONG).show();
                } else {
                    //以下是从游标中进行信息提取
                    cursor.moveToFirst();
                    String msg = statusMessage(cursor);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void downLoadApp() {
        final Request request = new Request(Uri.parse(url));
        //下载网络需求  手机数据流量、wifi
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
        //设置是否允许漫游网络 建立请求 默认true
        request.setAllowedOverRoaming(true);
        //设置通知类型
        setNotification(request);
        //设置下载路径
        setDownloadFilePath(request);
        //设置可被媒体文件扫描到
        request.allowScanningByMediaScanner();
        /*如果我们希望下载的文件可以被系统的Downloads应用扫描到并管理，
        我们需要调用Request对象的setVisibleInDownloadsUi方法，传递参数true。*/
        request.setVisibleInDownloadsUi(true);
        //设置请求的Mime
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        request.setMimeType(mimeTypeMap.getMimeTypeFromExtension(url));
        //开始下载
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Query query = new Query();
        query.setFilterById(lastDownloadId);
        Cursor cursor = downloadManager.query(query);
        if (!cursor.moveToFirst()) {// 没有记录
            PermissionCompat.Builder builder = new PermissionCompat.Builder(this);
            builder.addPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            builder.addPermissionRationale("say why need the permission");
            builder.addRequestPermissionsCallBack(new OnRequestPermissionsCallBack() {
                @Override
                public void onGrant() {
                    lastDownloadId = downloadManager.enqueue(request);
                }

                @Override
                public void onDenied(String permission) {
                    Log.e("update", permission + "Denied");
                }
            });
            builder.build().request();

        } else {
            //以下是从游标中进行信息提取
            cursor.moveToFirst();
            String msg = statusMessage(cursor);
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 查询状态
     */
    private String statusMessage(Cursor c) {
        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                return "Download failed";
            case DownloadManager.STATUS_PAUSED:
                return "Download paused";
            case DownloadManager.STATUS_PENDING:
                return "Download pending";
            case DownloadManager.STATUS_RUNNING:
                return "Download in progress!";
            case DownloadManager.STATUS_SUCCESSFUL:
                startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                return "Download finished";
            default:
                return "Unknown Information";
        }
    }


    /**
     * 设置状态栏中显示Notification
     */
    void setNotification(Request request) {
        //设置Notification的标题
        request.setTitle("纯纯写作");
        //设置描述
        request.setDescription("1.0.0");
        //request.setNotificationVisibility( Request.VISIBILITY_VISIBLE ) ;
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setNotificationVisibility( Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION ) ;
        //request.setNotificationVisibility( Request.VISIBILITY_HIDDEN ) ;
    }

    /**
     * 设置下载文件存储目录
     */
    void setDownloadFilePath(Request request) {
        /**
         * 方法1:
         * 目录: Android -> data -> com.app -> files -> Download -> 微信.apk
         * 这个文件是你的应用所专用的,软件卸载后，下载的文件将随着卸载全部被删除
         */

        //request.setDestinationInExternalFilesDir( this , Environment.DIRECTORY_DOWNLOADS ,  "微信.apk" );

        /**
         * 方法2:
         * 下载的文件存放地址  SD卡 download文件夹，pp.jpg
         * 软件卸载后，下载的文件会保留
         */
        //在SD卡上创建一个文件夹
        //request.setDestinationInExternalPublicDir(  "/mydownfile/"  , "weixin.apk" ) ;


        /**
         * 方法3:
         * 如果下载的文件希望被其他的应用共享
         * 特别是那些你下载下来希望被Media Scanner扫描到的文件（比如音乐文件）
         */
        //request.setDestinationInExternalPublicDir( Environment.DIRECTORY_MUSIC,  "笨小孩.mp3" );

        /**
         * 方法4
         * 文件将存放在外部存储的确实download文件内，如果无此文件夹，创建之，如果有，下面将返回false。
         * 系统有个下载文件夹，比如小米手机系统下载文件夹  SD卡--> Download文件夹
         */
        //创建目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();

        //设置文件存放路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "chunchunxiezuo.apk");
    }


}

