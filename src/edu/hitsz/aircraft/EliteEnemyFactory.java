package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class EliteEnemyFactory implements EnemyFactory{

    @Override
    public AbstractAircraft createEnemy(){
        double dirElite = Math.random();

        return new EliteEnemy((int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())) * 1,
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.2) * 1,
                dirElite >=0.5 ? 3 :-3,//精英机横飞方向随机
                8,
                50);
    }

}
