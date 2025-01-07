package org.poo.Merchants;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class MerchantAccounts {
    private HashMap<String, Merchant> merchAccounts;

    public MerchantAccounts() {
        merchAccounts = new HashMap<>();
    }

    public void addMerchAccount(String account, Merchant merchant) {
        merchAccounts.put(account, merchant);
    }
}
