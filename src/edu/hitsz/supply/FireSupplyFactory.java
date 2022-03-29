package edu.hitsz.supply;

public class FireSupplyFactory implements SupplyFactory{
    @Override
    public AbstractSupply createSupply(int LocationX,int LocationY){
        double dirSupply = Math.random();

        return new FireSupply(LocationX,LocationY,
                dirSupply  >= 0.5 ? -2 : 2,
                3);
    }
}
