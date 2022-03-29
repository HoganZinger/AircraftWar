package edu.hitsz.supply;

public class HpSupplyFactory implements SupplyFactory{
    @Override
    public AbstractSupply createSupply(int LocationX,int LocationY){
        double dirSupply = Math.random();

        return new HpSupply(LocationX,LocationY,
                dirSupply  >= 0.5 ? -2 : 2,
                3);
    }
}
