package org.poo.BankUsers;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public final class EmailDB {
    // the key here is the email address
    private final LinkedHashMap<String, User> associatedEmails;

    public EmailDB() {
        associatedEmails = new LinkedHashMap<>();
    }

    /**
     * adds a use to the email database
     *
     * @param user the user info
     */
    public void addUser(final User user) {
        associatedEmails.put(user.getEmail(), user);
    }

    /**
     * checks is an email corresponds to an existing user
     *
     * @param email the email address
     * @return the user if it exists, null otherwise
     */
    public User getUser(final String email) {
        return associatedEmails.getOrDefault(email, null);
    }


}
