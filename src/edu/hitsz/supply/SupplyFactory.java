package edu.hitsz.supply;

import edu.hitsz.basic.AbstractFlyingObject;

public interface SupplyFactory{
    AbstractSupply createSupply(int LocationX,int LocationY);
}
