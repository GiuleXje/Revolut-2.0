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

    public void addMerchant(Merchant merchant) {
        merchants.put(merchant.getName(), merchant);
    }

    public Merchant merchantInfo(String name) {
        return merchants.getOrDefault(name, null);
    }
}
