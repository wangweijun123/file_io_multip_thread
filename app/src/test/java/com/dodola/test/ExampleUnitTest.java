package com.dodola.test;

import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void useMultiThreadWriteFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeB();
            }
        }).start();

        /*try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        new Thread(new Runnable() {
            @Override
            public void run() {

                writeA();
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        readAB();
    }

    public void writeA() {
        // 访问一个文件必须枷锁,
        // 打开文件, 会产生fd，多个outputstrem就是多个fd，
        // 分别写，所以会覆盖掉,不管加不加锁
        File file = new File( "wangweijun.txt");
        try {
            // apend = ture 会追加
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
            for (int i = 0; i < 5; i++) {
                outputStream.writeUTF("1");
                Thread.sleep(1);
            }
            outputStream.close();
            out.println("A写完了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeB() {
        File file = new File( "wangweijun.txt");
        try {
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            for (int i = 0; i < 5; i++) {
                outputStream.writeUTF("2");
                Thread.sleep(2);
            }
            outputStream.close();
            out.println("B写完了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readAB() {
        File file = new File( "wangweijun.txt");
        int count = 0;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream inputStream = new DataInputStream(fileInputStream);
            while (true) {
                String s = inputStream.readUTF();
                out.println("s = " + s);
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println("读完了 count="+count);
    }




    @Test
    public void writeFile() {
        File file = new File( "wangweijun.txt");
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

    @Test
    public void readFile() {
        File file = new File( "wangweijun.txt");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte buff[] = new byte[1024];
            int len = -1;

            long start = System.currentTimeMillis();
            // PAGE 缓存存在
            len = fileInputStream.read(buff);
            out.println("第一次读完 spend time= " + (System.currentTimeMillis() - start) + " , len="+len);

            start = System.currentTimeMillis();
            len = fileInputStream.read(buff);
            out.println("第二次读完 spend time= " + (System.currentTimeMillis() - start) + " , len="+len);

            fileInputStream.close();
            out.println("读完了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    class TaskManger {
        //总的任务数量
        volatile int totalCount;
        List<Long> list;
        long startTime = System.currentTimeMillis();
        public TaskManger(int totalCount) {
            this.totalCount = totalCount;
            list = new ArrayList<>(totalCount);
        }

        void finish(long spendTime) {
            synchronized (this) {
                list.add(spendTime);
                totalCount --;
//                out.println("还剩下任务数量= " + totalCount);
                if (totalCount == 0) {
                    long temp = 0;
                    for (Long aLong : list) {
                        temp += aLong;
                    }
//                    out.println("所有任务都完成了平均耗时ms " + temp/list.size());
                    //
                    out.println("所有任务都完成了耗时ms " + (System.currentTimeMillis() - startTime));
                }
            }
        }
    }

    static class Task implements Runnable {
        TaskManger taskManger;
        String fileName;

        public Task(String fileName, TaskManger taskManger) {
            this.fileName = fileName;
            this.taskManger = taskManger;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            File file = new File(fileName);
            int readCount = 0;
            try {
                FileInputStream fileInputStream = new FileInputStream(file);

                byte buff[] = new byte[32 * 1024];
                int len = -1;
                while ((len = fileInputStream.read(buff)) != -1) {
                    readCount ++;
                }
                fileInputStream.close();
//                out.println("读完了 :"+fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long spendTime = (System.currentTimeMillis() - start);
//            out.println(fileName + " 读完 spend time= " + spendTime + " ms, readCount="+readCount);
            taskManger.finish(spendTime);
        }
    }
    @Test
    public void readFileUseThreadPool() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int taskCount = 10;
        TaskManger taskManger = new TaskManger(taskCount);
        for (int i = 0; i < taskCount; i++) {
            // 50 M
            executorService.execute(new Task(String.format("test%d.wmv", i), taskManger));
        }
        try {
            Thread.sleep(5 *1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void readSomeFileUseThreadPool() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        int taskCount = 10;
        TaskManger taskManger = new TaskManger(taskCount);
        for (int i = 0; i < taskCount; i++) {
            executorService.execute(new Task("test100.wmv", taskManger));
        }
        try {
            Thread.sleep(10 *1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}