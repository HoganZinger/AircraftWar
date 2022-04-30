package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;

import edu.hitsz.strategy.SingleShoot;
import edu.hitsz.supply.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;


import static edu.hitsz.aircraft.HeroAircraft.getHeroAircraft;
import static edu.hitsz.application.Main.object;

/**
 * 游戏主面板，游戏启动
 *
 * @author HoganZ
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
     * 敌机和道具的工厂
     */
    private final MobEnemyFactory mobEnemyFactory;
    private final EliteEnemyFactory eliteEnemyFactory;
    private final BossEnemyFactory bossEnemyFactory;
    private final BombSupplyFactory bombSupplyFactory;
    private final FireSupplyFactory fireSupplyFactory;
    private final HpSupplyFactory hpSupplyFactory;

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    private int cycleDuration = 120;
    private int cycleTime = 0;
    /**
     * 难度与音效设定
     */
    private int difficulty = 0;
    /**
     * 7个位置分别对应7个音频
     * 0:bgm
     * 1:bgm_boss
     * 2:bullet
     * 3:bullet_hit
     * 4:bomb_explosion
     * 5:get_supply
     * 6:game_over
     */
    private MusicThread[] musicThreads = new MusicThread[7];
    private boolean needMusic = false;

    public void setMusicUsage(boolean needMusic) {
        this.needMusic = needMusic;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getScore() {
        return score;
    }

    private volatile static Game game;
    public static Game getGame(){
        if(game==null){
            synchronized (Game.class) {
                if(game==null){
                    game = new Game();
                }
            }
        }
        return game;
    }

    /**
     * 指示变量
     * 指示子弹的发射、敌机的产生
     *
     */
    private int eliteFlag = 0;
    private int mobFlag = 0;
    private int enemyShootFlag = 0;
    private int heroShootFlag = 0;
    private int shootStrategyFlag = 0;
    private int mobCount = 5;
    private int eliteCount = 8;
    private int enemyShootCount = 8;
    private int heroShootCount = 2;
    private int shootStrategyCount = 2000;
    private int bossScoreThreshold = 1000;
    private boolean bossExistence = false;

    /**
     * 观察者设置
     */
    private List<AbstractFlyingObject> SubscriberList = new ArrayList<>();
    public void addSubscriber(AbstractFlyingObject flyingObject){
        SubscriberList.add(flyingObject);
    }
    public void removeSubscriber(AbstractFlyingObject flyingObject){
        SubscriberList.remove(flyingObject);
    }
    public void notifyAll(int number){
        for(AbstractFlyingObject object : SubscriberList){
            object.update();
        }
    }

    public Game() {

        heroAircraft = getHeroAircraft();
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
    public void action(){

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
                        AbstractAircraft enemyAircraft = produceEnemy(0);
                        enemyAircrafts.add(enemyAircraft);
                        addSubscriber(enemyAircraft);

                    }
                }

                if (eliteFlag == eliteCount) {
                    eliteFlag = 0;
                    if(enemyAircrafts.size() < enemyMaxNumber){
                        AbstractAircraft enemyAircraft = produceEnemy(1);
                        enemyAircrafts.add(enemyAircraft);
                        addSubscriber(enemyAircraft);

                    }
                }

                if(score != 0 && score % bossScoreThreshold == 0){
                    if(!bossExistence){
                        AbstractAircraft enemyAircraft = produceEnemy(2);
                        enemyAircrafts.add(enemyAircraft);
                        bossExistence = true;
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

            //设置射击策略
            setStrategyAction();

            //音乐循环播放
            if(needMusic){
                musicCheck();
            }

            // 后处理
            postProcessAction();

            //每个时刻重绘界面
            repaint();

            /// 游戏结束检查
            if (heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
                if (needMusic) {
                    for (int i = 0; i < 6; i++) {
                        if (Objects.nonNull(musicThreads[i])) {
                            musicThreads[i].setValid(false);
                        }
                    }
                    musicThreads[6] = new MusicThread("src/videos/game_over.wav");
                    musicThreads[6].start();
                    try {
                        musicThreads[6].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (object) {
                    object.notify();
                }
            }
        };

            /**
             * 以固定延迟时间进行执行
             * 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
             */
            executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

            if(needMusic){
                musicThreads[0] = new MusicThread("src/videos/bgm.wav");
                musicThreads[0].start();
        }
        }

        private void musicCheck() {
            //背景音乐循环播放
            if (!musicThreads[0].isAlive() && !bossExistence) {
                musicThreads[0] = new MusicThread("src/videos/bgm.wav");
                musicThreads[0] .start();
            }
            //boss音乐
            if (!bossExistence) {

                if (Objects.nonNull(musicThreads[1])) {
                    musicThreads[1].setValid(false);
                    musicThreads[1] = null;
                }
            }
            else {
                if (Objects.isNull(musicThreads[1]) || !musicThreads[1].isAlive()) {
                    MusicThread bossBgm = new MusicThread("src/videos/bgm_boss.wav");
                    musicThreads[1] = bossBgm;
                    bossBgm.start();
                }
                if (Objects.nonNull(musicThreads[0])) {
                    musicThreads[0].setValid(false);
                }
            }
        }

        private AbstractAircraft produceEnemy(int choice){
            int mobChoice = 0;
            int eliteChoice = 1;
            int bossChoice = 2;
            if (choice == eliteChoice){
                return eliteEnemyFactory.createEnemy();}
            else if(choice == mobChoice){
                return mobEnemyFactory.createEnemy();}
            else if(choice == bossChoice){
                return bossEnemyFactory.createEnemy();
            }
            else {return null;}
        }

        private AbstractSupply produceSupply(int locationX, int locationY) {
            long rand = System.currentTimeMillis();
            //随机产生三种道具之一
            int cntN1 = 5;
            int cntN2 = 2;
            int cntN3 = 3;
            if (rand % cntN1 == 1) {
                return hpSupplyFactory.createSupply(locationX, locationY);
            } else if (rand % cntN1 == cntN2) {
                return bombSupplyFactory.createSupply(locationX, locationY);
            } else if (rand % cntN1 == cntN3) {
                return fireSupplyFactory.createSupply(locationX, locationY);
            } else {return null;}
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
                if (needMusic){
                    musicThreads[2] = new MusicThread("src/videos/bullet.wav");
                    musicThreads[2].start();
                }
                for(AbstractAircraft enemy : enemyAircrafts){
                    enemyBullets.addAll(enemy.shoot(0, 0, 0, 0, 0, 0));
                }
                enemyShootFlag = 0;

            }
            if(heroShootFlag == heroShootCount) {
                // 英雄射击
                heroBullets.addAll(heroAircraft.shoot(0, 0, 0 ,0 ,0, 0));
                heroShootFlag = 0;
            }

        }

        private void bulletsMoveAction() {
            for (BaseBullet bullet : heroBullets) {
                bullet.forward();
            }
            for (BaseBullet bullet : enemyBullets) {
                addSubscriber(bullet);
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
                    if (needMusic){
                        musicThreads[3] = new MusicThread("src/videos/bullet_hit.wav");
                        musicThreads[3].start();
                    }
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
                        if (needMusic){
                            musicThreads[3] = new MusicThread("src/videos/bullet_hit.wav");
                            musicThreads[3].start();
                        }
                        enemyAircraft.decreaseHp(bullet.getPower());
                        bullet.vanish();
                        removeSubscriber(bullet);
                        if (enemyAircraft.notValid()) {
                            // 获得分数，产生道具补给
                            score += 10;
                            if (enemyAircraft instanceof EliteEnemy) {
                                int locationX = enemyAircraft.getLocationX();
                                int locationY = enemyAircraft.getLocationY();
                                AbstractSupply abstractSupply = produceSupply(locationX, locationY);
                                if (!Objects.isNull(abstractSupply)) {
                                    supplies.add(abstractSupply);
                                }
                                removeSubscriber(enemyAircraft);
                            }
                            if(enemyAircraft instanceof MobEnemy){
                                removeSubscriber(enemyAircraft);
                            }
                            //Boss敌机被击落后获得较多分数，产生多次道具补给
                            if(enemyAircraft instanceof BossEnemy){
                                score += 190;
                                int count = 5;
                                for(int i = 0; i < count; i++){
                                    int locationX = enemyAircraft.getLocationX();
                                    int locationY = (int)(Math.random()*enemyAircraft.getLocationY()*0.2);
                                    AbstractSupply abstractSupply = produceSupply(locationX, locationY);
                                    if (!Objects.isNull(abstractSupply)) {
                                        supplies.add(abstractSupply);
                                    }
                                }
                                bossExistence = false;
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
                if (supply.notValid()){
                    continue;}

                if (heroAircraft.crash(supply)) {
                    //当英雄机满血时道具不会生效
                    if(supply instanceof HpSupply && heroAircraft.getHp() == heroAircraft.getMaxHp()){
                        break;
                    }
                    //炸弹生效时通知全体观察者
                    if(supply instanceof BombSupply){
                        notifyAll(0);
                    }
                    if(needMusic){
                        if(supply instanceof BombSupply){
                            musicThreads[4] = new MusicThread("src/videos/bomb_explosion.wav");
                            musicThreads[4].start();
                        }
                        else{
                            musicThreads[5] = new MusicThread("src/videos/get_supply.wav");
                            musicThreads[5].start();
                        }
                    }
                    supply.supplyFunction(heroAircraft);
                    if(supply instanceof FireSupply){ shootStrategyFlag = time; }
                    supply.vanish();
                }
            }

        }

        /**
         * 火力道具时效处理
         * 经过固定时间长度后英雄机射击模式重新变为单射
         */
        private void setStrategyAction(){
            if(time - shootStrategyFlag >= shootStrategyCount){
                SingleShoot singleShoot = new SingleShoot();
                heroAircraft.setShootStrategy(singleShoot);
                shootStrategyFlag = 0;
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
