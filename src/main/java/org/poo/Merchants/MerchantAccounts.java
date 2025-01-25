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

    /**
     * adds a merchant account to the database
     * @param account
     * @param merchant
     */
    public void addMerchAccount(final String account, final Merchant merchant) {
        merchAccounts.put(account, merchant);
    }
}
