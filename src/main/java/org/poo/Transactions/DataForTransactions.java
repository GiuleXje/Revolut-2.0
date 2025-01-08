package org.poo.Transactions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Data
public final class DataForTransactions {
    private String command;
    private int timestamp;
    private double amount;
    private String currency;
    private String payerIBAN;
    private String receiverIBAN;
    private String dataType;
    private String description;
    private String account;
    private String cardNumber;
    private String email;
    private String merchant;
    private List<String> accounts;
    private double splitAmount;
    private String transferType;
    private double interestRate;
    private String newPlan;
    private String location;
    private List<Double> payEach;

    /**
     * includes a command
     *
     * @param command1 name of the command
     * @return -
     */
    public DataForTransactions withCommand(final String command1) {
        this.command = command1;
        return this;
    }

    /**
     * includes a timestamp
     *
     * @param timestamp1 -
     * @return -
     */
    public DataForTransactions withTimestamp(final int timestamp1) {
        this.timestamp = timestamp1;
        return this;
    }

    /**
     * includes the amount
     *
     * @param am -
     * @return -
     */
    public DataForTransactions withAmount(final double am) {
        this.amount = am;
        return this;
    }

    /**
     * includes the currency
     *
     * @param curr -
     * @return -
     */
    public DataForTransactions withCurrency(final String curr) {
        this.currency = curr;
        return this;
    }

    /**
     * includes the payer's IBAN
     *
     * @param payIBAN -
     * @return -
     */
    public DataForTransactions withPayerIBAN(final String payIBAN) {
        this.payerIBAN = payIBAN;
        return this;
    }

    /**
     * includes the receiver's IBAN
     *
     * @param receiveIBAN -
     * @return -
     */
    public DataForTransactions withReceiverIBAN(final String receiveIBAN) {
        this.receiverIBAN = receiveIBAN;
        return this;
    }

    /**
     * includes the data Type
     *
     * @param dataTypes -
     * @return -
     */
    public DataForTransactions withDataType(final String dataTypes) {
        this.dataType = dataTypes;
        return this;
    }

    /**
     * includes the description
     *
     * @param desc -
     * @return -
     */
    public DataForTransactions withDescription(final String desc) {
        this.description = desc;
        return this;
    }

    /**
     * includes the account
     *
     * @param accountMain -
     * @return -
     */
    public DataForTransactions withAccount(final String accountMain) {
        this.account = accountMain;
        return this;
    }

    /**
     * includes the card number
     *
     * @param cardNum -
     * @return -
     */
    public DataForTransactions withCardNumber(final String cardNum) {
        this.cardNumber = cardNum;
        return this;
    }

    /**
     * includes the email
     *
     * @param emailAdd -
     * @return -
     */
    public DataForTransactions withEmail(final String emailAdd) {
        this.email = emailAdd;
        return this;
    }

    /**
     * includes the merchant
     *
     * @param merch -
     * @return -
     */
    public DataForTransactions withMerchant(final String merch) {
        this.merchant = merch;
        return this;
    }

    /**
     * includes the accounts
     *
     * @param accountsInvolved -
     * @return -
     */
    public DataForTransactions withAccounts(final List<String> accountsInvolved) {
        this.accounts = accountsInvolved;
        return this;
    }

    /**
     * includes the split amount
     *
     * @param splitAm -
     * @return -
     */
    public DataForTransactions withSplitAmount(final double splitAm) {
        this.splitAmount = splitAm;
        return this;
    }

    /**
     * include the transfer type
     *
     * @param transfer -
     * @return -
     */
    public DataForTransactions withTransferType(final String transfer) {
        this.transferType = transfer;
        return this;
    }

    /**
     * includes the interest rate
     *
     * @param interest -
     * @return -
     */
    public DataForTransactions withInterestRate(final double interest) {
        this.interestRate = interest;
        return this;
    }

    /**
     * includes the new plan
     * @param newPlan1
     * -
     * @return
     * -
     */
    public DataForTransactions withNewPlan(final String newPlan1) {
        this.newPlan = newPlan1;
        return this;
    }

    /**
     * sets the location of a cash withdrawal
     * @param location1 -
     * @return -
     */
    public DataForTransactions withLocation(final String location1) {
        this.location = location1;
        return this;
    }

    /**
     * sets the amount that has to be paid by each bank account
     * @param payEach1 -
     * @return -
     */
    public DataForTransactions withPayEach(final List<Double> payEach1) {
        this.payEach = payEach1;
        return this;
    }
}
