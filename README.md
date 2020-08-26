# rabbitID
兔子ID生成器
有以下几个优点
1. id生成是连续的，递增的，对索引比较友好。
2. 使用预申请号段，可以在redis挂掉的情况下使用一段时间。
3. 使用双Segment号段，性能好


# 核心流程讲解

![免子ID生成器获取ID流程](https://img-blog.csdnimg.cn/20200807085318227.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTEyOTYxNjU=,size_16,color_FFFFFF,t_70)

此图加载如果慢的话可以看我的博客 https://blog.csdn.net/u011296165/article/details/107854375


# 性能
在本地电脑跑了跑10万个ID大约是在7秒左右。

# 使用方法

