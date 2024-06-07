package com.solbot.sniper.data;

public class SwapResult {
    private final String owner;
    private final String tokenMint;
    private final String solTokenAccount;
    private final String mintTokenAccount;
    private final TxResult txResult;

    public SwapResult(String owner, String tokenMint, String solTokenAccount, String mintTokenAccount, TxResult txResult) {
        this.owner = owner;
        this.tokenMint = tokenMint;
        this.solTokenAccount = solTokenAccount;
        this.mintTokenAccount = mintTokenAccount;
        this.txResult = txResult;
    }

    public String getOwner() {
        return owner;
    }

    public String getTokenMint() {
        return tokenMint;
    }

    public String getSolTokenAccount() {
        return solTokenAccount;
    }

    public String getMintTokenAccount() {
        return mintTokenAccount;
    }

    public TxResult getTxResult() {
        return txResult;
    }

    @Override
    public String toString() {
        return "SwapResult{" +
                "owner='" + owner + '\'' +
                ", tokenMint='" + tokenMint + '\'' +
                ", solTokenAccount='" + solTokenAccount + '\'' +
                ", mintTokenAccount='" + mintTokenAccount + '\'' +
                ", txResult='" + txResult + '\'' +
                '}';
    }
}
