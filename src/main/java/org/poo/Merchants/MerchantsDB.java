package org.poo.Merchants;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class MerchantsDB {
    private HashMap<String, Merchant> merchants;

    public MerchantsDB() {
        merchants = new HashMap<>();
    }

    /**
     * adds a merchant into the database
     * @param merchant -
     */
    public void addMerchant(final Merchant merchant) {
        merchants.put(merchant.getName(), merchant);
    }

    /**
     * returns a merchant info
     * @param name -
     * @return -
     */
    public Merchant merchantInfo(final String name) {
        return merchants.getOrDefault(name, null);
    }
}
