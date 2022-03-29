package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.supply.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static edu.hitsz.aircraft.HeroAircraft.getHeroAircraft;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;

    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractSupply> supplies;

    private int enemyMaxNumber = 5;

    private boolean gameOverFlag = false;
    private int score = 0;
    private int time = 0;

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    /**
     * 敌机和道具的工厂
     */
    private final MobEnemyFactory mobEnemyFactory;
    private final EliteEnemyFactory eliteEnemyFactory;
    private final BossEnemyFactory bossEnemyFactory;
    private final BombSupplyFactory bombSupplyFactory;
    private final FireSupplyFactory fireSupplyFactory;
    private final HpSupplyFactory hpSupplyFactory;

    private int cycleDuration = 120;//适当调低时间周期方便后续难度调节
    private int cycleTime = 0;
    private int eliteFlag = 0;
    private int mobFlag = 0;
    private int enemyShootFlag = 0;
    private int heroShootFlag = 0;

    //用以控制游戏难度设计
    private int mobCount = 4;//控制普通敌机产生频率
    private int eliteCount = 6;//控制精英敌机产生频率
    private int enemyShootCount = 7;//控制精英敌机射击频率
    private int heroShootCount = 2;//控制英雄敌机射击频率
    private int BossNum = 300;//BOSS机生成阈值


    public Game() {

        heroAircraft = getHeroAircraft();//单例模式实现英雄飞机初始化
        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        supplies = new LinkedList<>();

        //Scheduled 线程池，用于定时任务调度
        executorService = new ScheduledThreadPoolExecutor(1);

        //初始化敌机和道具工厂
        mobEnemyFactory = new MobEnemyFactory();
        eliteEnemyFactory = new EliteEnemyFactory();
        bossEnemyFactory = new BossEnemyFactory();
        hpSupplyFactory = new HpSupplyFactory();
        bombSupplyFactory = new BombSupplyFactory();
        fireSupplyFactory = new FireSupplyFactory();

        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {

        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {

            time += timeInterval;

            // 周期性执行（控制频率）
            if (timeCountAndNewCycleJudge()) {
                System.out.println(time);

                // 新敌机产生
                if (mobFlag == mobCount) {
                    mobFlag = 0;
                    if (enemyAircrafts.size() < enemyMaxNumber) {
                        AbstractAircraft enemyAircraft = ProduceEnemy(0);
                        enemyAircrafts.add(enemyAircraft);

                    }
                }

                if (eliteFlag == eliteCount) {
                    eliteFlag = 0;
                    if(enemyAircrafts.size() < enemyMaxNumber){
                        AbstractAircraft enemyAircraft = ProduceEnemy(1);
                        enemyAircrafts.add(enemyAircraft);

                    }

                }


                eliteFlag++;
                mobFlag++;
                enemyShootFlag++;
                heroShootFlag++;

                shootAction();
            }

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            //道具移动
            suppliesMoveAction();

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            //每个时刻重绘界面
            repaint();

            // 游戏结束检查
            if (heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
            }

        };

        /**
         * 以固定延迟时间进行执行
         * 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }


    private AbstractAircraft ProduceEnemy(int choice){
        if (choice == 1)
            return eliteEnemyFactory.createEnemy();
        else if(choice == 0)
            return mobEnemyFactory.createEnemy();
        else return null;
    }

    private AbstractSupply produceSupply(int locationX, int locationY) {
        long rand = System.currentTimeMillis();
        //随机产生三种道具之一
        if (rand % 5 == 1) {
            return hpSupplyFactory.createSupply(locationX, locationY);
        } else if (rand % 5 == 2) {
            return bombSupplyFactory.createSupply(locationX, locationY);
        } else if (rand % 5 == 3) {
            return fireSupplyFactory.createSupply(locationX, locationY);
        } else return null; //代表未掉落道具
    }

    //***********************
    //      Action 各部分
    //***********************

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration && cycleTime - timeInterval < cycleTime) {
            // 跨越到新的周期
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }


    private void shootAction() {
        if(enemyShootFlag == enemyShootCount) {
            //敌机射击
            for(AbstractAircraft enemy : enemyAircrafts)
                enemyBullets.addAll(enemy.shoot());
            enemyShootFlag = 0;


        }
        if(heroShootFlag == heroShootCount) {
            // 英雄射击
            heroBullets.addAll(heroAircraft.shoot());
            heroShootFlag = 0;
        }

    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void suppliesMoveAction(){
        for (AbstractSupply supply : supplies){
            supply.forward();
        }

    }
    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }
        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // 获得分数，产生道具补给
                        score += 10;
                        if (enemyAircraft instanceof EliteEnemy) {
                            int locationX = enemyAircraft.getLocationX();
                            int locationY = enemyAircraft.getLocationY();
                            AbstractSupply abstractSupply = produceSupply(locationX, locationY);
                            if (!Objects.isNull(abstractSupply)) {//如果返回null代表没掉落道具
                                supplies.add(abstractSupply);
                            }
                        }
                    }
                }
                    // 英雄机 与 敌机 相撞，均损毁
                    if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                        enemyAircraft.vanish();
                        heroAircraft.decreaseHp(Integer.MAX_VALUE);
                    }
                }
            }

            // 我方获得道具，道具生效
            for (AbstractSupply supply : supplies) {
                if (supply.notValid())
                    continue;

                if (heroAircraft.crash(supply)) {
                    if(supply instanceof HpSupply && heroAircraft.getHp() == heroAircraft.getMaxHp()){
                        break;
                    }//当英雄机满血时hp道具不会起效
                    supply.SupplyFunction(heroAircraft);
                    supply.vanish();
                }
            }

        }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 删除无效的道具
     * 4. 检查英雄机生存
     * <p>
     * 无效的原因可能是撞击或者飞出边界
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        supplies.removeIf(AbstractFlyingObject::notValid);
    }


    //***********************
    //      Paint 各部分
    //***********************

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param  g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        //道具层不做特殊要求
        paintImageWithPositionRevised(g, supplies);
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);

        paintImageWithPositionRevised(g, enemyAircrafts);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        //绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }


}
