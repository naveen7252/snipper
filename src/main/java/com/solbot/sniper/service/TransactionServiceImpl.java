package com.solbot.sniper.service;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.data.SolanaTransaction;
import com.solbot.sniper.data.TransactionParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Value("${solana.buy.max.retry}")
    private int buyMaxRetry;

    @Value("${solana.sell.max.retry}")
    private int sellMaxRetry;

    @Value("${solana.retry.interval}")
    private int retryInterval;

    private final SolanaRpcClient rpcClient;

    public TransactionServiceImpl(SolanaRpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public String submitTransaction(SolanaTransaction transaction, TransactionParams transactionParams) {
        return rpcClient.getApi().sendTransaction(transaction, transactionParams.getPreflightCommitment(), transactionParams.getSkipPreflight());
    }

    @Override
    public String retryTransaction(String txSignature, SolanaTransaction transaction, TransactionParams transactionParams) {
        return null;
    }
}
