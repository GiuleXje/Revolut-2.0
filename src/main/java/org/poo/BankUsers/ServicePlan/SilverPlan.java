package org.poo.BankUsers.ServicePlan;

public class SilverPlan implements ServicePlan {
    static final double FEE = 0.001;
    static final double FEE_CAP = 500;

    @Override
    public double fee(double amount) {
        if (amount < FEE_CAP)
            return 0;
        return amount * FEE;
    }
}
