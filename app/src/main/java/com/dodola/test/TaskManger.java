package com.dodola.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.out;

public class TaskManger {
    ExecutorService executorService;
    //总的任务数量
    volatile int totalCount;
    int originCount;
    List<Long> list;
    long startTime = 0;
    boolean isStartTimeInited = false;
    public TaskManger(int totalCount) {
        this.totalCount = totalCount;
        originCount = totalCount;
        list = new ArrayList<>(totalCount);
        executorService = Executors.newFixedThreadPool(4);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getOriginCount() {
        return originCount;
    }

    public void addTask(String taskName) {
        if (!isStartTimeInited) {
            isStartTimeInited = true;
            startTime = System.currentTimeMillis();
        }
        executorService.execute(new Task(taskName, this));
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
                long spendTotalTime = (System.currentTimeMillis() - startTime);
                out.println("所有任务都完成了耗时ms " +  spendTotalTime
                        + ", 平均耗时:"+(spendTotalTime/originCount));
            }
        }
    }
}
