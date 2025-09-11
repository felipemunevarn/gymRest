package com.epam.gym.workload.util;

import java.util.UUID;

public class TransactionUtil {

    /**
     * Generate a unique transaction ID
     */
    public static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}