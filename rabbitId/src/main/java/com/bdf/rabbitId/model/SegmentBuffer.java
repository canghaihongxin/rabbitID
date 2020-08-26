package com.bdf.rabbitId.model;

import com.bdf.rabbitId.BufferAllocator;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;

/**
 * author: 田培融
 */
@Data
@Accessors(chain = true)
public class SegmentBuffer {

    /**
     *  ID实例KEY
     */
    private String key;
    /**
     * ID增长步长
     */
    private int step;
    /**
     *  最大ID号
     */
    private long max;
    /**
     * 触发申请号段因子
     */
    private int factor;
    /**
     * 损耗额度
     */
    private int wasteQuota;
    /**
     * 当前Buffer的index
     */
    private int currentPos;
    /**
     * 当前最新的ID值
     */
    private long currentValue;
    /**
     * 下一个segment是否处于可切换状态
     */
    private boolean nextReady;
    /**
     * 下一个Buffer申请的最大ID值
     */
    private long nextMax;

    @Data
    public static class Segment{
        /**
         * ID值
         */
        private AtomicLong value;
        /**
         * 当前号段最大值
         */
        private volatile long max;
        private BufferAllocator bufferAllocator;

        public Segment(BufferAllocator bufferAllocator) {
            value = new AtomicLong(0);
            this.bufferAllocator = bufferAllocator;
        }

        public long getId(){
            return this.getValue().getAndIncrement();
        }

        public long getIdle(){
            return this.getMax() - getValue().get();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Segment(")
                    .append("value:").append(value)
                    .append(",max:").append(max)
                    .append(")");
            return sb.toString();
        }

    }
}
