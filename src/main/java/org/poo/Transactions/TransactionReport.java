package org.poo.Transactions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

interface TransactionStrategy {
    /**
     * generates a JSON containing specific data about each operation
     * performed in a bank account / user
     *
     * @param data contains data needed for the transaction
     * @return an ObjectNode of the report
     */
    ObjectNode generateReport(DataForTransactions data);
}

class NewAccReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description", "New account created");
        return output;
    }
}

class SendMoneyReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description", data.getDescription());
        output.put("senderIBAN", data.getPayerIBAN());
        output.put("receiverIBAN", data.getReceiverIBAN());
        output.put("amount", data.getAmount()
                + " " + data.getCurrency());
        output.put("transferType", data.getTransferType());
        return output;
    }
}

class NewCardReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("account", data.getAccount());
        output.put("card", data.getCardNumber());
        output.put("cardHolder", data.getEmail());
        output.put("description", "New card created");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class InsufficientFundsReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("description", "Insufficient funds");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class CardDestroyedReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("account", data.getAccount());
        output.put("card", data.getCardNumber());
        output.put("cardHolder", data.getEmail());
        output.put("description", "The card has been destroyed");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class CardPaymentReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("amount", data.getAmount());
        output.put("commerciant", data.getMerchant());
        output.put("description", "Card payment");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class WarningReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description",
                "You have reached the minimum amount of funds, "
                        + "the card will be frozen");
        return output;
    }
}

class UnderMinAmountReport implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description",
                "The card is frozen");
        return output;
    }
}

class FailedSplittingBill implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("amount", data.getSplitAmount());
        output.put("currency", data.getCurrency());
        output.put("description", "Split payment of "
                + String.format("%.2f", data.getAmount())
                + " " + data.getCurrency());
        output.put("error", "Account " + data.getAccount()
                + " has insufficient funds for a split payment.");
        ArrayNode accs = new ObjectMapper().createArrayNode();
        for (String acc : data.getAccounts()) {
            accs.add(acc);
        }
        output.set("involvedAccounts", accs);
        output.put("splitPaymentType", data.getSplitType());
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class InvolvedInBill implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description", "Split payment of "
                + String.format("%.2f", data.getAmount())
                + " " + data.getCurrency());
        output.put("currency", data.getCurrency());
        output.put("amount", data.getSplitAmount());
        ArrayNode involved = new ObjectMapper().createArrayNode();
        List<String> accounts = data.getAccounts();
        for (String account : accounts) {
            involved.add(account);
        }
        output.set("involvedAccounts", involved);
        output.put("splitPaymentType", data.getSplitType());
        return output;
    }
}

class NoAcc implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description", "Account not found");
        return output;
    }
}

class ChangeInterest implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("description", "Interest rate of the account changed to "
                + data.getInterestRate());
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class ChangeOfPlan implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description", "Upgrade plan");
        output.put("accountIBAN", data.getAccount());
        output.put("newPlanType", data.getNewPlan());
        return output;
    }
}

class TooYoung implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("description", "You don't have the minimum age required.");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class CashWithdrawal implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("amount", Math.round(data.getAmount() * 100) / 100.0);
        output.put("description", "Cash withdrawal of " + data.getAmount());
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class InterestRate implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("amount", data.getAmount());
        output.put("currency", data.getCurrency());
        output.put("description", "Interest rate income");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class NoClassicAccount implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("description", "You do not have a classic account.");
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class CustomSplitPayment implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("timestamp", data.getTimestamp());
        output.put("description", "Split payment of "
        + String.format("%.2f", data.getAmount()) + " " + data.getCurrency());
        output.put("splitPaymentType", data.getSplitType());
        output.put("currency", data.getCurrency());
        ArrayNode forEach = new ObjectMapper().createArrayNode();
        for (double sum : data.getPayEach()) {
            forEach.add(sum);
        }
        output.set("amountForUsers", forEach);
        ArrayNode involved = new ObjectMapper().createArrayNode();
        List<String> accounts = data.getAccounts();
        for (String account : accounts) {
            involved.add(account);
        }
        output.set("involvedAccounts", involved);
        return output;
    }
}

class FailedCustomSplit implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        ArrayNode forEach = new ObjectMapper().createArrayNode();
        for (double sum : data.getPayEach()) {
            forEach.add(sum);
        }
        output.set("amountForUsers", forEach);
        output.put("currency", data.getCurrency());
        output.put("description", "Split payment of " + String.format("%.2f", data.getAmount())
                + " " + data.getCurrency());
        output.put("error", "Account " + data.getAccount() + " has insufficient " +
                "funds for a split payment.");
        ArrayNode involved = new ObjectMapper().createArrayNode();
        for (String account : data.getAccounts()) {
            involved.add(account);
        }
        output.set("involvedAccounts", involved);
        output.put("splitPaymentType", data.getSplitType());
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

class RejectSplit implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode report = new ObjectMapper().createObjectNode();
        ArrayNode forEach = new ObjectMapper().createArrayNode();
        for (double sum : data.getPayEach()) {
            forEach.add(sum);
        }
        report.set("amountForUsers", forEach);
        report.put("currency", data.getCurrency());
        report.put("description", "Split payment of "
                + String.format("%.2f", data.getSplitAmount()) + " " + data.getCurrency());
        report.put("error", "One user rejected the payment.");
        ArrayNode involved = new ObjectMapper().createArrayNode();
        for (String account : data.getAccounts()) {
            involved.add(account);
        }
        report.set("involvedAccounts", involved);
        report.put("splitPaymentType", data.getSplitType());
        report.put("timestamp", data.getTimestamp());
        return report;
    }
}

class WithdrawSavings implements TransactionStrategy {
    @Override
    public ObjectNode generateReport(final DataForTransactions data) {
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("amount", data.getAmount());
        output.put("classicAccountIBAN", data.getAccount());
        output.put("description", "Savings withdrawal");
        output.put("savingsAccountIBAN", data.getSavingsAccount());
        output.put("timestamp", data.getTimestamp());
        return output;
    }
}

public final class TransactionReport {

    /**
     * executed the specified transaction based on the command
     *
     * @param data info needed to generate specific reports
     * @return an object node containing data of the transaction
     */
    public ObjectNode executeOperation(final DataForTransactions data) {
        TransactionStrategy transaction = createTransactionReport(data.getCommand());
        if (transaction != null) {
            return transaction.generateReport(data);
        }
        return null;
    }

    private TransactionStrategy createTransactionReport(final String command) {
        if (command != null) {
            return switch (command) {
                case "addAccount" -> new NewAccReport();
                case "sendMoney" -> new SendMoneyReport();
                case "createCard" -> new NewCardReport();
                case "noFunds" -> new InsufficientFundsReport();
                case "deleteCard" -> new CardDestroyedReport();
                case "payOnline" -> new CardPaymentReport();
                case "minAmount" -> new WarningReport();
                case "frozen" -> new UnderMinAmountReport();
                case "splitBill" -> new InvolvedInBill();
                case "poorFriend" -> new FailedSplittingBill();
                case "noAcc" -> new NoAcc();
                case "changeInterest" -> new ChangeInterest();
                case "tooYoung" -> new TooYoung();
                case "changeOfPlan" -> new ChangeOfPlan();
                case "cashWithdrawal" -> new CashWithdrawal();
                case "interest" -> new InterestRate();
                case "noClassic" -> new NoClassicAccount();
                case "customSplit" -> new CustomSplitPayment();
                case "poorFriendV2" -> new FailedCustomSplit();
                case "rejectSplit" -> new RejectSplit();
                case "withdrawSavings" -> new WithdrawSavings();
                default -> null;
            };
        }
        return null;
    }
}
