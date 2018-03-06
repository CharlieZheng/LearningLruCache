package com.cgf.learninglrucache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String bitmapUrl = "http://a1.att.hudong.com/13/97/01300000329092124720974128477.jpg";
    private static final int DOWNLOAD_IMAGE = 1;
    private ImageView imageView;
    private ImageDownloader imageDownloader = new ImageDownloader();

    private static class MyHandler extends Handler {
        private WeakReference<MainActivity> ref;

        public MyHandler(MainActivity mainActivity) {
            this.ref = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_IMAGE:
                    ref.get().imageView.setImageBitmap((Bitmap) msg.obj);
                    break;
            }
        }
    }

    private Handler handler = new MyHandler(this);

    class BitmapThread extends Thread {
        private String bitmapUrl;

        BitmapThread(String bitmapUrl) {
            this.bitmapUrl = bitmapUrl;
        }

        @Override
        public void run() {

            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(bitmapUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
                imageDownloader.addBitmapToMemory("bitmap", bitmap);
                handler.obtainMessage(DOWNLOAD_IMAGE, bitmap).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void remove(View view) {
        imageDownloader.removeBitmapFromMemory("bitmap");
    }

    public void showBitmap(View view) {
        Bitmap bitmap = imageDownloader.getBitmapFromMemCache("bitmap");
        if (bitmap == null) {
            new BitmapThread(bitmapUrl).start();
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
    }
}
