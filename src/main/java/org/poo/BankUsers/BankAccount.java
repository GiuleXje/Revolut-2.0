package org.poo.BankUsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.ExchangeRate.Pair;
import org.poo.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

interface InterestStrategy {
    /**
     * calculates the account's balance based on its interest rate
     *
     * @param balance      balance at the time of this calculation
     * @param interestRate the rate
     * @return the new balance
     */
    double calculateInterest(double balance, double interestRate);
}

class SavingsInterest implements InterestStrategy {
    @Override
    public double calculateInterest(final double balance, final double interestRate) {
        return balance * interestRate;
    }
}

class ClassicInterest implements InterestStrategy {
    @Override
    public double calculateInterest(final double balance, final double interestRate) {
        return balance;
    }
}

@Setter
@Getter
public class BankAccount {
    private static final double INIT_LIMIT = 500;

    private double balance;
    private final String email;
    private final String currency;
    private final String accountType;
    private final int timestamp;
    private final InterestStrategy interestStrategy;
    private double interestRate;
    private final String iBAN;
    private String alias;
    private LinkedHashMap<String, Card> cards;
    private double minAmount;
    private ArrayList<ObjectNode> report;
    private LinkedHashMap<Integer, Pair<String, Double>> merchants;
    private int transactions;
    private boolean usedFoodCB;
    private boolean usedTechCB;
    private boolean usedClothesCB;
    private User owner;
    private HashSet<User> managers;
    private HashSet<User> employees;
    private double spendingLimit;
    private double depositLimit;
    // the key is the card number
    private HashMap<String, User> businessCards;

    public BankAccount(final String email, final String currency, final String accountType,
                       final int timestamp, final double interestRate) {
        balance = 0;
        this.email = email;
        this.currency = currency;
        this.accountType = accountType;
        this.timestamp = timestamp;
        this.interestRate = interestRate;
        this.interestStrategy = interestRate > 0 ? new SavingsInterest()
                : new ClassicInterest();
        this.iBAN = Utils.generateIBAN();
        cards = new LinkedHashMap<>();
        alias = "";
        minAmount = 0;
        report = new ArrayList<>();
        merchants = new LinkedHashMap<>();
        transactions = 0;
        usedFoodCB = false;
        usedTechCB = false;
        usedClothesCB = false;
    }

    public BankAccount(final String email, final String currency, final User owner,
                       final int timestamp) {
        balance = 0;
        this.email = email;
        this.currency = currency;
        this.owner = owner;
        this.timestamp = timestamp;
        iBAN = Utils.generateIBAN();
        cards = new LinkedHashMap<>();
        alias = "";
        minAmount = 0;
        interestStrategy = new ClassicInterest();
        interestRate = 0;
        report = new ArrayList<>();
        merchants = new LinkedHashMap<>();
        transactions = 0;
        usedFoodCB = false;
        usedTechCB = false;
        usedClothesCB = false;
        managers = new HashSet<>();
        employees = new HashSet<>();
        accountType = "business";
        spendingLimit = depositLimit = INIT_LIMIT;
        businessCards = new LinkedHashMap<>();
    }
    /**
     * Computes the account's balance for a given interest rate
     *
     * @return The new account balance
     */
    public double calculateInterest() {
        return interestStrategy.calculateInterest(balance, interestRate);
    }

    /**
     * adds a card to a user's bank account
     *
     * @param card the new card
     */
    public void addCard(final Card card) {
        cards.put(card.getNumber(), card);
    }

    /**
     * adds fund to a bank account balance
     *
     * @param funds to be added
     */
    public void addFunds(final double funds) {
        balance += funds;
    }

    /**
     * deletes a card related to a bank account
     *
     * @param card the card to be deleted
     */
    public void deleteCard(final Card card) {
        cards.remove(card.getNumber());
    }

    /**
     * deletes all cards of a bank account
     */
    public void deleteAllCards() {
        cards.clear();
    }

    /**
     * subtracts an amount as a result of a payment
     * this also includes the fee for every transaction
     * @param amount the amount to be subtracted
     */
    public void pay(final double amount) {
        balance -= amount;
    }

    /**
     * set a bank account's alias
     *
     * @param newAlias the new bank account name
     */
    public void changeAlias(final String newAlias) {
        alias = newAlias;
    }

    /**
     * makes a card unavailable
     *
     * @param cardNumber the card
     */
    public void freezeThisCard(final String cardNumber) {
        cards.get(cardNumber).setStatus("frozen");
    }

    /**
     * adds a new transaction report resulted form a payment or account operation
     *
     * @param report1 the new transaction report
     */
    public void addReport(final ObjectNode report1) {
        this.report.add(report1);
    }

    /**
     * gets all the reports generated in a time sequence
     *
     * @param start the start timestamp
     * @param end   the end timestamp
     * @return the transactions made in that period of time
     */
    public ArrayNode generateReport(final int start, final int end) {
        ArrayNode transactions = new ObjectMapper().createArrayNode();
        for (ObjectNode transaction : report) {
            int timestamp1 = Integer.parseInt(transaction.get("timestamp").asText());
            if (timestamp1 >= start && timestamp1 <= end) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    public void increaseTransactions() {
        transactions++;
    }

    /**
     * changes account's interest rate
     *
     * @param interestRate1 the new rate
     */
    public void changeInterest(final double interestRate1) {
        interestRate = interestRate1;
    }


    /**
     * adds a new employee to a business account
     * @param employee -
     */
    public void addEmployee(final User employee) {
        employees.add(employee);
    }

    /**
     * adds a new manager to a business account
     * @param manager -
     */
    public void addManager(final User manager) {
        managers.add(manager);
    }

    /**
     * adds a new card related to a certain user that created it
     * @param card -
     * @param businessCard -
     */
    public void addBusinessCard(final String card, final User businessCard) {
        businessCards.put(card, businessCard);
    }
}
