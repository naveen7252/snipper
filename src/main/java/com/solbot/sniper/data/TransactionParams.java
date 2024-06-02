package com.solbot.sniper.data;

import com.paymennt.solanaj.api.rpc.types.LatestBlockHash;
import com.paymennt.solanaj.api.rpc.types.PreflightCommitment;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;

public class TransactionParams {

    private final SolanaCommitment commitment;
    private final PreflightCommitment preflightCommitment;
    private final boolean skipPreflight;
    private final LatestBlockHash latestBlockHash;

    public TransactionParams(SolanaCommitment commitment,
                             PreflightCommitment preflightCommitment,
                             boolean skipPreflight,
                             LatestBlockHash latestBlockHash) {
        this.commitment = commitment;
        this.preflightCommitment = preflightCommitment;
        this.skipPreflight = skipPreflight;
        this.latestBlockHash = latestBlockHash;
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
}
