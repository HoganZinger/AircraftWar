package edu.hitsz.basic;

/**
 * 观察者模式的订阅者抽象接口
 */
public interface SubscriberInterface {
    /**
     * 遇到炸弹后的反应
     */
    void update();
}
