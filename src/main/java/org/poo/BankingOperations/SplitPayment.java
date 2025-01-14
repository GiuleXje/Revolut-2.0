package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.CustomSplit;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public final class SplitPayment implements BankingOperations {
    /**
     * handles a split payment
     * @param command -
     * @return -
     */
    public ObjectNode customSplitPayment(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        if (commandInput.getSplitPaymentType().equals("custom")) {
            List<String> accounts = commandInput.getAccounts();
            List<Double> payEach = commandInput.getAmountForUsers();
            double amount = commandInput.getAmount();
            String currency = commandInput.getCurrency();
            int timestamp = commandInput.getTimestamp();

            CustomSplit customSplit = new CustomSplit(accounts, payEach, currency,
                    timestamp, amount, commandInput.getSplitPaymentType());
            command.getActiveSplits().add(customSplit);
        } else {
            List<String> accounts = commandInput.getAccounts();
            double amount = commandInput.getAmount();
            String currency = commandInput.getCurrency();
            int timestamp = commandInput.getTimestamp();
            List<Double> payEach = new ArrayList<>();
            double toPay = amount / accounts.size();
            for (int i = 0; i < accounts.size(); i++) {
                payEach.add(toPay);
            }
            CustomSplit customSplit = new CustomSplit(accounts, payEach, currency,
                    timestamp, amount, "equal");
            command.getActiveSplits().add(customSplit);
        }
        return null;
    }

    @Override
    public ObjectNode execute(final BankOpData command) {
        return customSplitPayment(command);
    }
}
