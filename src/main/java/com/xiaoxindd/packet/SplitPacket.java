package com.xiaoxindd.packet;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 需求：参照微信、支付宝红包，将金额拆分为相应人数的红包，且每个红包不得超过总金额的90%。
 * （由于都是在电话里口述，不知道有没有听错需求，姑且就当它是正确的吧）
 * 
 * 由于该需求会产生较多0.01的红包
 * 
 * @author TPig
 * @date 2017年1月20日 下午9:19:03
 * @version 1.0
 */
public class SplitPacket {

    /**
     * 单元测试
     */
    @org.junit.Test
    public void testSplitRedPacket() throws Exception {
        long startTime = System.currentTimeMillis();
        int sum = 0;
        BigDecimal amount;
        int pCount = 2;

        for (double i = 1; i < 10;) {
            sum = 0;
            amount = new BigDecimal(String.valueOf(i));
            int amountInt = amount.multiply(new BigDecimal("100")).intValue();

            int[] packets = splitRedPacket(amountInt, pCount);
            for (int packet : packets) {
                sum += packet;
            }
            BigDecimal bSum = new BigDecimal(Double.valueOf(sum / 100.0));

            assertCompare(bSum, amount);

            if(pCount < 20) {
                pCount++;
            } else {
                pCount = 2;
            }
            i = new BigDecimal(String.valueOf(i)).add(new BigDecimal("0.1")).doubleValue();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("总消耗时间：" + (endTime - startTime) + "毫秒");
    }

    private void assertCompare(BigDecimal sum, BigDecimal amount) {
        // System.out.println(sum.doubleValue() + " " + amount.doubleValue());
        if(sum.doubleValue() != amount.doubleValue()) {
            throw new RuntimeException("拆分后总金额不相等，计算出错\nbd1=" + sum.doubleValue() + " bd2=" + amount.doubleValue());
        }
    }

    /**
     * 拆分红包
     * 
     * @param amount 总金额
     * @param pCount 人数
     * @return
     * @throws Exception
     */
    public int[] splitRedPacket(int amount, int pCount) throws Exception {

        // 总金额
        /*
         * TODO 总金额直接使用分计算
         */
        // 先将总金额单位改为分
        int amountInt = amount;

        // 验证最小红包金额
        verifyMinPacket(amountInt, pCount);

        int[] redPackets = new int[ pCount ];

        Random r = new Random(System.nanoTime());
        // 最大红包金额
        int max = amountInt;
        // 已发放总金额
        int sum = 0;
        // 每份的金额
        int part = 0;

        for (int i = 0; i < pCount - 1; i++) {
            // 预留金额
            int preAmount = preAmount(max, amountInt, pCount - i - 1);
            max -= preAmount;

            part = r.nextInt(max) + 1;

            // 余额
            max -= part;

            // 补偿预留金额
            max += preAmount;

            sum += part;
            // BigDecimal bPart = new BigDecimal(Double.valueOf(part / 100.0));
            redPackets[i] = part;

        }
        // 最后一份红包
        // redPackets[pCount - 1] = new BigDecimal(Double.valueOf((amountInt - sum) / 100.0));
        redPackets[pCount - 1] = amountInt - sum;
        return redPackets;
    }

    /**
     * 计算预留金额
     * 
     * @param balance 余额
     * @param amount 总金额
     * @param pCount 剩余人数
     * @return
     */
    private int preAmount(int balance, int amount, int pCount) {
        // 打9折
        int $9 = (int) (amount * 0.9);
        if(balance > $9) {
            // 余额必须大于总金额的90% 并且 10%足够剩余人数分
            int $1 = (int) (amount * 0.1);
            if($1 / pCount >= 1) {
                return $1;

            } else {
                // 10%不够分的时候
                return pCount;
            }
        }
        return pCount;
    }

    /**
     * 验证最小红包金额
     * 
     * @param amount 总金额
     * @param pCount 人数
     * @return
     * @throws Exception
     */
    private boolean verifyMinPacket(int amount, int pCount) throws Exception {
        if(amount / pCount >= 1) {
            return true;
        }
        throw new Exception("单个红包不能小于0.01");
    }

}
