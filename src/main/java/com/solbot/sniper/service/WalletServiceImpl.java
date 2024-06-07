package com.solbot.sniper.service;

import com.paymennt.crypto.bip32.wallet.AbstractWallet;
import com.paymennt.crypto.bip32.wallet.key.HdPrivateKey;
import com.paymennt.solanaj.data.SolanaAccount;
import com.paymennt.solanaj.wallet.SolanaWallet;
import com.solbot.sniper.data.WalletInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger LOG = LoggerFactory.getLogger(WalletServiceImpl.class);

    private final Map<Integer, SolanaAccount> accountMapByIndex;
    private final Map<String, SolanaAccount> accountMapByAddress;

    @Autowired
    public WalletServiceImpl(WalletInfo walletInfo) {
        accountMapByIndex = new LinkedHashMap<>(walletInfo.getTotalAccountsInitialized());
        accountMapByAddress = new LinkedHashMap<>(walletInfo.getTotalAccountsInitialized());
        populateAccounts(walletInfo);
    }

    @Override
    public SolanaAccount getAccount(int accountIndex) {
        return accountMapByIndex.get(accountIndex);
    }

    @Override
    public SolanaAccount getAccount(String account) {
        return accountMapByAddress.get(account);
    }

    private void populateAccounts(WalletInfo walletInfo) {
        final SolanaWallet solanaWallet = walletInfo.getWallet();
        final int totalAccounts = walletInfo.getTotalAccountsInitialized();
        for (int i = 1; i <= totalAccounts; i++) {
            HdPrivateKey privateKey = solanaWallet.getPrivateKey(i - 1, AbstractWallet.Chain.EXTERNAL, null);
            final SolanaAccount account = new SolanaAccount(privateKey);
            accountMapByIndex.put(i, account);
            accountMapByAddress.put(account.getPublicKey().toBase58(), account);
        }
        List<String> addresses = accountMapByIndex.values().stream().map(account -> account.getPublicKey().toBase58()).collect(Collectors.toList());
        LOG.info("******************************************* ");
        LOG.info("******** Wallet Accounts Populated ******** ");
        addresses.forEach(LOG::info);
        LOG.info("******************************************* ");
    }
}
