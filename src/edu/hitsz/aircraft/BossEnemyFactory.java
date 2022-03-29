package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

public class BossEnemyFactory implements EnemyFactory{
    @Override
    public AbstractAircraft createEnemy(){
        return new BossEnemy(1,
                2,
                3,
                0,
                600);
    }
}
