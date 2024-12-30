package org.poo.BankUsers;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public final class CardDB {
    // the key is the card's number
    private LinkedHashMap<String, BankAccount> associatedCards;

    /**
     * adds a card to a bank account
     *
     * @param card    -
     * @param account -
     */
    public void addCard(final Card card, final BankAccount account) {
        associatedCards.put(card.getNumber(), account);
    }

    /**
     * deletes a card from a bank account
     *
     * @param cardNumber -
     */
    public void deleteCard(final String cardNumber) {
        associatedCards.remove(cardNumber);
    }

    public CardDB() {
        associatedCards = new LinkedHashMap<>();
    }
}
