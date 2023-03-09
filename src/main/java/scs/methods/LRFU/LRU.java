package scs.methods.LRFU;

import scs.controller.ConfigPara;
import scs.controller.OperWaitQueue;

/**
 * @ClassName LRU
 * @Description ...
 * @Author @WZhang
 * @Date 2023/3/8 12:52
 * @Version 1.0
 */
public class LRU {
    private static long[] funcStartExecTime = new long[]{0, 0, 0, 0, 0, 0, 0};
    private static long startTime = System.currentTimeMillis();

    public LRU() {
        //funcStartExecTime = new long[]{0, 0, 0, 0, 0, 0, 0};
        //startTime = System.currentTimeMillis();
    }

    public static void run(Integer sid) {
        if(ConfigPara.waitQueue.size() != 0) {
            if (ConfigPara.funcFlagArray[sid - 1] == 0) {
                long tempTimeInterval = Long.MAX_VALUE;
                Integer tempSid = 0;
                System.out.println("-------------waitQueue not empty,Add waitQueue-----------");
                ConfigPara.waitQueue.add(sid);

                for(int i = 0; i < ConfigPara.funcFlagArray.length; i++) {
                    if(ConfigPara.funcFlagArray[i] == 1 && funcStartExecTime[i] < tempTimeInterval) {
                        tempTimeInterval = funcStartExecTime[i];
                        tempSid = i + 1;
                    }
                }
                if(tempSid != 0) {
                    System.out.println("-----------release-----------");
                    OperWaitQueue.releaseFunc(tempSid);

                    for(int j = 0; j < ConfigPara.waitQueue.size(); j++) {
                        if(ConfigPara.funcCapacity[ConfigPara.waitQueue.peek()-1] <= ConfigPara.getRemainMemCapacity()) {
                            Integer tempSid1 = ConfigPara.waitQueue.poll();
                            OperWaitQueue.execFunc(tempSid1);
                        } else {
                            break;
                        }
                    }
                }
            }
            else {
                System.out.println("---------------waitQueue exec-----------------");
                long t1 = System.currentTimeMillis();
                funcStartExecTime[sid-1] = t1 - startTime;
                OperWaitQueue.execFunc(sid);
            }
        }
        else {
            if(ConfigPara.funcCapacity[sid-1] <= ConfigPara.getRemainMemCapacity()) {
                System.out.println("show capacity: " + ConfigPara.funcCapacity[sid-1] + " " + ConfigPara.getRemainMemCapacity());
                long t1 = System.currentTimeMillis();
                funcStartExecTime[sid-1] = t1 - startTime;
                OperWaitQueue.execFunc(sid);
            }
            else {
                System.out.println("-------------capacity not enough,Add waitQueue-----------");
                ConfigPara.waitQueue.add(sid);
            }
        }
    }
}
