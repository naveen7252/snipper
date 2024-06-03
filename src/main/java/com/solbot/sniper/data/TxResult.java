package com.solbot.sniper.data;

import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;

public class TxResult {
    private final String signature;
    private final boolean isConfirmed;
    private final ConfirmationStatus confirmationStatus;

    public TxResult(String signature,
                    boolean isConfirmed,
                    ConfirmationStatus confirmationStatus
                    ) {
        this.signature = signature;
        this.confirmationStatus = confirmationStatus;
        this.isConfirmed = isConfirmed;
    }

    public String getSignature() {
        return signature;
    }

    public ConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public static TxResult getNotConfirmedTx(String signature) {
        return new TxResult(signature, false, new ConfirmationStatus(null, 0, false));
    }

    public record ConfirmationStatus(SolanaCommitment confirmation,
                                      long slot,
                                      boolean isError) {
    }

    @Override
    public String toString() {
        return "TxResult{" +
                "signature='" + signature + '\'' +
                ", isConfirmed=" + isConfirmed +
                ", confirmationStatus=" + confirmationStatus +
                '}';
    }
}
