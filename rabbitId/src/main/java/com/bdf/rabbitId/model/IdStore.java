package com.bdf.rabbitId.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用于持久化存储
 * @author 田培融
 */
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class IdStore {
    private Integer id;
    /**
     *  ID实例KEY
     */
    private String key;
    /**
     *  名称描述
     */
    private String name;
    /**
     *  最大ID号
     */
    private long max;

    /**
     * 申请号段ID增长步长
     */
    private int step;
    /**
     * 触发申请号段因子,100以内整数，表示当前Buffer的空闲未消耗的ID占比，空闲ID小于阈值时，触发下个Buffer预申请号段
     */
    private int factor;
    /**
     * 损耗额度，当服务重启后，优先从镜像的SegmentBuffer副本加载, 在上次最后一个ID基础上，设置一定的损耗额度，防止ID重复
     */
    private int wasteQuota;

    /**
     * 更新时间戳
     */
    private Long ts;

    private IdStore(Builder builder) {
        setId(builder.id);
        setKey(builder.key);
        setName(builder.name);
        setMax(builder.max);
        setStep(builder.step);
        setFactor(builder.factor);
        setWasteQuota(builder.wasteQuota);
        setTs(builder.ts);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static final class Builder {
        private Integer id;
        private String key;
        private String name;
        private long max;
        private int step;
        private int factor;
        private int wasteQuota;
        private Long ts;

        public Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder key(String val) {
            key = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder max(long val) {
            max = val;
            return this;
        }

        public Builder step(int val) {
            step = val;
            return this;
        }

        public Builder factor(int val) {
            factor = val;
            return this;
        }

        public Builder wasteQuota(int val){
            wasteQuota = val;
            return this;
        }

        public Builder ts(Long val) {
            ts = val;
            return this;
        }

        public IdStore build() {
            return new IdStore(this);
        }
    }
}
