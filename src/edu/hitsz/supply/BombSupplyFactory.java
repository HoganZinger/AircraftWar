package edu.hitsz.supply;

import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.application.Game;

public class BombSupplyFactory implements SupplyFactory{

    @Override
    public AbstractSupply createSupply(int LocationX,int LocationY){
        double dirSupply = Math.random();

        return new BombSupply(LocationX,LocationY,
                dirSupply  >= 0.5 ? -2 : 2,
                3);
    }
}
