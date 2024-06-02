package com.solbot.sniper.service;

import com.paymennt.solanaj.data.SolanaTransaction;
import com.solbot.sniper.data.TransactionParams;

public interface TransactionService {

    String submitTransaction(SolanaTransaction transaction, TransactionParams transactionParams);

    String retryTransaction(String txSignature, SolanaTransaction transaction, TransactionParams transactionParams);
}
