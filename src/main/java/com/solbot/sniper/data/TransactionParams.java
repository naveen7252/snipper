package com.solbot.sniper.data;

import com.paymennt.solanaj.api.rpc.types.LatestBlockHash;
import com.paymennt.solanaj.api.rpc.types.PreflightCommitment;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;
import com.solbot.sniper.constant.TransactionType;

public class TransactionParams {

    private final SolanaCommitment commitment;
    private final PreflightCommitment preflightCommitment;
    private final boolean skipPreflight;
    private final LatestBlockHash latestBlockHash;
    private final TransactionType transactionType;

    public TransactionParams(SolanaCommitment commitment,
                             PreflightCommitment preflightCommitment,
                             boolean skipPreflight,
                             LatestBlockHash latestBlockHash) {
        this(commitment, preflightCommitment, skipPreflight, latestBlockHash, TransactionType.OTHER);
    }

    public TransactionParams(SolanaCommitment commitment,
                             PreflightCommitment preflightCommitment,
                             boolean skipPreflight,
                             LatestBlockHash latestBlockHash,
                             TransactionType transactionType) {
        this.commitment = commitment;
        this.preflightCommitment = preflightCommitment;
        this.skipPreflight = skipPreflight;
        this.latestBlockHash = latestBlockHash;
        this.transactionType = transactionType;
    }

    public SolanaCommitment getCommitment() {
        return commitment;
    }

    public PreflightCommitment getPreflightCommitment() {
        return preflightCommitment;
    }

    public boolean getSkipPreflight() {
        return skipPreflight;
    }

    public LatestBlockHash getLatestBlockHash() {
        return latestBlockHash;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }
}
