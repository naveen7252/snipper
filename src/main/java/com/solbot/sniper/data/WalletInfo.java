package com.solbot.sniper.data;

import com.paymennt.solanaj.wallet.SolanaWallet;

public class WalletInfo {
    private final SolanaWallet wallet;
    private final int totalAccountsInitialized;

    public WalletInfo(SolanaWallet wallet,
                      int totalAccountsInitialized) {
        this.wallet = wallet;
        this.totalAccountsInitialized = totalAccountsInitialized;
    }

    public SolanaWallet getWallet() {
        return wallet;
    }

    public int getTotalAccountsInitialized() {
        return totalAccountsInitialized;
    }
}
