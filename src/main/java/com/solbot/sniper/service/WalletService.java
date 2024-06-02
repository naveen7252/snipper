package com.solbot.sniper.service;

import com.paymennt.solanaj.data.SolanaAccount;

public interface WalletService {
    SolanaAccount getAccount(int account);
}
