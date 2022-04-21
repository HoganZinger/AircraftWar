package edu.hitsz.basic;

import edu.hitsz.bullet.BaseBullet;

import java.util.List;
/**
 * 射击策略的抽象接口
 *
 * @author hitsz
 */
public interface ShootStrategy {
    abstract List<BaseBullet> shoot();
}
