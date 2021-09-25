package com.dodola.test;

import java.io.File;
import java.io.FileInputStream;

import static java.lang.System.out;

public class Task  implements Runnable {

    TaskManger taskManger;
    String fileName;
    int buffer = 32;
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
            byte buff[] = new byte[buffer * 1024];
            int len = -1;
            while ((len = fileInputStream.read(buff)) != -1) {
                readCount ++;
            }
            fileInputStream.close();
            long spendTime = (System.currentTimeMillis() - start);
            out.println(fileName + " 读完 spend time= " + spendTime +
                    " ms, tid="+Thread.currentThread().getId()+", buffer:"+buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        taskManger.finish(1);
    }
}
