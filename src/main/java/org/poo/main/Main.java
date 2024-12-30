package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.poo.BankUsers.User;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.AccountDB;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.AliasDB;
import org.poo.BankingOperations.BankOpData;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     *
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        var sortedFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles())).
                sorted(Comparator.comparingInt(Main::fileConsumer))
                .toList();

        for (File file : sortedFiles) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(CheckerConstants.TESTS_PATH + filePath1);
        ObjectInput inputData = objectMapper.readValue(file, ObjectInput.class);

        ArrayNode output = objectMapper.createArrayNode();

        // get all the data needed from the input
        UserInput[] usersInfo = inputData.getUsers();
        ExchangeInput[] exRates = inputData.getExchangeRates();
        CommandInput[] commands = inputData.
                getCommands();

        // create a database containing all the users' data,
        // accessible by their email address
        EmailDB emailDB = new EmailDB();
        // create a database that can return each IBAN's user
        IBANDB ibanDB = new IBANDB();
        // create a database that can return a card's bank account
        CardDB cardDB = new CardDB();
        // set a new exchange rate graph
        ExchangeRate exchangeRate = new ExchangeRate(exRates);
        // set a new alias database
        AliasDB aliasDB = new AliasDB();
        // set a new accounts(IBAN) database
        AccountDB accountDB = new AccountDB();

        //reset the card and IBAN generator
        Utils.resetRandom();

        // get the users
        for (UserInput user : usersInfo) {
            emailDB.addUser(new User(user.getFirstName(),
                    user.getLastName(), user.getEmail()));
        }

        // get each action
        for (CommandInput command : commands) {
            BankOpData commandHandler = new BankOpData(command, emailDB, ibanDB, cardDB,
                    exchangeRate, aliasDB, accountDB);
            commandHandler.execute(); // handle the given command

            // output for JSON, if needed
            if (commandHandler.getReturnVal() != null) {
                output.add(commandHandler.getReturnVal());
            }
        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    /**
     * Method used for extracting the test number from the file name.
     *
     * @param file the input file
     * @return the extracted numbers
     */
    public static int fileConsumer(final File file) {
        String fileName = file.getName()
                .replaceAll(CheckerConstants.DIGIT_REGEX,
                        CheckerConstants.EMPTY_STR);
        return Integer.parseInt(fileName.substring(0, 2));
    }
}
