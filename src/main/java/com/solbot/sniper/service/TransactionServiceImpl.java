package com.solbot.sniper.service;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.LatestBlockHash;
import com.paymennt.solanaj.api.rpc.types.RpcResponse;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;
import com.paymennt.solanaj.data.SolanaTransaction;
import com.solbot.sniper.data.TransactionParams;
import com.solbot.sniper.data.TxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

import static com.solbot.sniper.service.UtilityService.getLatestBlockHash;
import static com.solbot.sniper.service.UtilityService.isBlockHashValid;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Value("${solana.tx.max.retry}")
    private int maxRetry;

    @Value("${solana.retry.interval}")
    private int retryInterval;

    private final SolanaRpcClient rpcClient;

    public TransactionServiceImpl(SolanaRpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public TxResult submitTransaction(SolanaTransaction transaction, TransactionParams transactionParams) {
        String signature = rpcClient.getApi().sendTransaction(transaction, transactionParams.getPreflightCommitment(), transactionParams.getSkipPreflight());
        TxResult txResult = getTransactionConfirmation(signature);
        return txResult.isConfirmed() ? txResult : retryTransaction(signature, transaction, transactionParams);
    }

    private TxResult retryTransaction(String txSignature, SolanaTransaction transaction, TransactionParams transactionParams) {
        TxResult txResult = TxResult.getNotConfirmedTx(txSignature);
        final SolanaCommitment solanaCommitment = transactionParams.getCommitment();
        LatestBlockHash latestBlockHash = transactionParams.getLatestBlockHash();
        String signature = txSignature;
        int retry = 0;
        boolean isProcessed = false;
        String lastValidBlockHash;
        while (retry < maxRetry) {
            txResult = getTransactionConfirmation(signature);
            if (txResult.isConfirmed()) {
                LOG.info("Transaction successfully processed by the validator [{}] ", txResult);
                isProcessed = true;
                break;
            } else {
                if (retry % 10 == 0/* && !isBlockHashValid(rpcClient, latestBlockHash.getLatestBlockHash(), solanaCommitment)*/) {
                    lastValidBlockHash = getLatestBlockHash(rpcClient, solanaCommitment).getLatestBlockHash();
                    transaction.setRecentBlockHash(lastValidBlockHash);
                    signature = rpcClient.getApi().sendTransaction(transaction, transactionParams.getPreflightCommitment(), transactionParams.getSkipPreflight());
                } else {
                    signature = rpcClient.getApi().sendTransaction(transaction, transactionParams.getPreflightCommitment(), transactionParams.getSkipPreflight());
                }
                retry++;
                LOG.info("Retry [{} --> {}]", retry, signature);
            }
            try {
                //noinspection BusyWait
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
        if (!isProcessed) {
            LOG.error("Transaction not processed after {} retries, timing out ", maxRetry);
        }
        return txResult;

    }

    @Override
    public TxResult getTransactionConfirmation(String signature) {
        final RpcResponse<Object> rpcResponse = rpcClient.getApi().getSignatureStatusesWithoutParsing(List.of(signature));
        final LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) rpcResponse.getResult();
        final List<LinkedHashMap<String, Object>> valueList = (List<LinkedHashMap<String, Object>>) result.get("value");
        if (valueList == null || valueList.isEmpty()) {
            return TxResult.getNotConfirmedTx(signature);
        }
        final LinkedHashMap<String, Object> confirmationStatusMap = valueList.get(0);
        if (confirmationStatusMap == null) {
            return TxResult.getNotConfirmedTx(signature);
        }
        LOG.info("Confirmation = {}", confirmationStatusMap);
        final String confirmationStatus = (String) confirmationStatusMap.get("confirmationStatus");
        int slot = (int) confirmationStatusMap.get("slot");
        final LinkedHashMap<String, Object> statusMap = (LinkedHashMap<String, Object>) confirmationStatusMap.get("status");
        Object err = statusMap.get("Err");
        if (err != null) {
            LOG.error("transaction is failed, pls check [signature={}]", signature);
        }
        return new TxResult(signature, true, new TxResult.ConfirmationStatus(SolanaCommitment.valueOf(confirmationStatus), slot, err != null));
    }
}
