package org.poo.Merchants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Merchant {
    final String name;
    final int id;
    final String account;
    final String type;
    final String cashbackPlan;

    public Merchant(final String name, final int id, final String account,
                    final String type, final String cashbackPlan) {
        this.name = name;
        this.id = id;
        this.account = account;
        this.type = type;
        this.cashbackPlan = cashbackPlan;
    }
}
