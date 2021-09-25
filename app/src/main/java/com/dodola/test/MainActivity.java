package com.dodola.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.out;

public class MainActivity extends AppCompatActivity {
    TaskManger taskManger;
    int i;

    static final int fileCount = 30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskManger = new TaskManger(fileCount);

        int processors = Runtime.getRuntime().availableProcessors();
        out.println("核心 processors =  "+processors + ", 0.7倍数 = " + (processors*0.7));
    }
    public void readFile(View view) {
        String absolutePath = getExternalCacheDir().getAbsolutePath();
        int originCount = taskManger.getOriginCount();
        out.println(" originCount =  "+originCount);
        for (int i = 0; i < originCount; i++) {
            taskManger.addTask(absolutePath + "/"+String.format("test%d.wmv", i));

            // 文件名一样的话，有page cach z在，第二次从从内存缓存读了哈，所以块
//            taskManger.addTask(absolutePath + "/"+String.format("test%d.wmv", 0));
        }
    }

    public void jumpSecond(View view) {
        add();
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(intent);
    }


    private  int add() {
        int b = 10;
        synchronized(this) {
            i ++;
        }
        int c = 110;
        return i;
    }

    public void writeFile(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int j = 0; j < fileCount; j++) {
                    copyAssertFile2CacheFile(String.format("test%d.wmv", j));
                }

            }
        }).start();
    }

    private void copyAssertFile2CacheFile(String sdcardFileName) {
        long start = System.currentTimeMillis();
        try {
            InputStream inputStream =  getAssets().open("test.wmv");
            File sdcardFile = new File(getExternalCacheDir(), sdcardFileName);
            FileOutputStream outputStream = new FileOutputStream(sdcardFile);
            byte buff[] = new byte[32 * 1024];
            int len = -1;
            while ((len = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
            inputStream.close();
            outputStream.close();
            out.println("copy success " + sdcardFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println(" spend time= " + (System.currentTimeMillis() - start));
    }



    private void read() {
        File file = new File( getCacheDir(),"wangweijun.txt");
        long start = System.currentTimeMillis();
        int len = -1;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte buff[] = new byte[4096];
            len = fileInputStream.read(buff);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println(" spend time= " + (System.currentTimeMillis() - start) + " , len="+len);
    }

    private void write() {
        File file = new File( getCacheDir(),"wangweijun.txt");
        try {
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            for (int i = 0; i < 10000; i++) {
                outputStream.writeUTF("aaaaaaaaaaaa");
            }
            outputStream.close();
            out.println("写完了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}