package org.poo.BankUsers;

import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

@Getter
@Setter
public final class Card {
    private final String number;
    private String status;
    private final boolean oneTime;

    public Card() {
        number = Utils.generateCardNumber();
        status = "active";
        oneTime = false;
    }

    public Card(final boolean oneTime) {
        number = Utils.generateCardNumber();
        status = "active";
        this.oneTime = oneTime;
    }

    /**
     * @return returns if the card is one time
     */
    public boolean getOneTime() {
        return oneTime;
    }


}
