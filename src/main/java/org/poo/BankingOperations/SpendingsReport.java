package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.Pair;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpendingsReport implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        IBANDB ibanDB = command.getIbanDB();
        int start = commandInput.getStartTimestamp();
        int end = commandInput.getEndTimestamp();
        String account = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(account);
        if (user == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "spendingsReport");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("timestamp", commandInput.getTimestamp());
            out.put("description", "Account not found");
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }
        BankAccount bankAccount = user.getBankAccounts().getOrDefault(account, null);
        if (bankAccount == null) {
            return null;
        }

        if (bankAccount.getAccountType().equals("savings")) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "spendingsReport");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("error",
                    "This kind "
                            + "of report is not "
                            + "supported for a saving account");
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("command", "spendingsReport");
        ObjectNode out = new ObjectMapper().createObjectNode();
        out.put("IBAN", account);
        out.put("balance", bankAccount.getBalance());
        out.put("currency", bankAccount.getCurrency());
        ArrayNode transactions = new ObjectMapper().createArrayNode();
        HashMap<String, Double> merchants = new HashMap<>();

        for (int time : bankAccount.getMerchants().keySet()) {
            if (time >= start && time <= end) {
                ObjectNode payment = new ObjectMapper().createObjectNode();
                payment.put("timestamp", time);
                payment.put("description", "Card payment");
                double amount = bankAccount.getMerchants().get(time).getValue();
                String merch = bankAccount.getMerchants().get(time).getKey();
                payment.put("amount", amount);
                payment.put("commerciant", merch);
                if (merchants.containsKey(merch)) {
                    double toAdd = merchants.get(merch) + amount;
                    merchants.remove(merch);
                    merchants.put(merch, toAdd);
                } else {
                    merchants.put(merch, amount);
                }
                transactions.add(payment);
            } else if (time > end) {
                break;
            }
        }

        out.set("transactions", transactions);
        ArrayList<Pair<String, Double>> sortedMerch = new ArrayList<>();
        List<Map.Entry<String, Double>> merchantEntries =
                new ArrayList<>(merchants.entrySet());

        // sort by key(merchants)
        merchantEntries.sort((entry1, entry2) ->
                entry1.getKey().compareTo(entry2.getKey()));

        for (Map.Entry<String, Double> entry : merchantEntries) {
            sortedMerch.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        ArrayNode commerciants = new ObjectMapper().createArrayNode();
        for (Pair<String, Double> entry : sortedMerch) {
            ObjectNode commerciant = new ObjectMapper().createObjectNode();
            commerciant.put("commerciant", entry.getKey());
            commerciant.put("total", entry.getValue());
            commerciants.add(commerciant);
        }

        out.set("commerciants", commerciants);
        output.set("output", out);
        output.put("timestamp", commandInput.getTimestamp());
        return output;
    }
}
