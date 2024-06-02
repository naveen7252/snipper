package com.solbot.sniper.constant;

import com.paymennt.solanaj.api.rpc.types.PreflightCommitment;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;

public class CurrentSolanaCommitment {

    public static SolanaCommitment getCommitment() {
        return SolanaCommitment.processed;
    }

    public static PreflightCommitment getPreflightCommitment() {
        return PreflightCommitment.processed;
    }

    private CurrentSolanaCommitment() {
    }
}
