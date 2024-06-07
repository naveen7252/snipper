package com.solbot.sniper.service;

import com.paymennt.solanaj.data.SolanaTransaction;
import com.solbot.sniper.data.TransactionParams;
import com.solbot.sniper.data.TxResult;

public interface TransactionService {
    TxResult submitTransaction(SolanaTransaction transaction, TransactionParams transactionParams);
    TxResult getTransactionConfirmation(String signature);
}
