package com.solbot.sniper.config;

import com.paymennt.crypto.bip32.Network;
import com.paymennt.solanaj.api.rpc.Cluster;
import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.wallet.SolanaWallet;
import com.solbot.sniper.data.WalletInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Value("${solana.cluster}")
    private String clusterName;

    @Value("${solana.network}")
    private String networkName;

    @Value("${walletKey}")
    private String walletKey;

    @Value("${solana.total.accounts.initialize}")
    private int totalAccountsToInitialize;

    @Bean
    public SolanaRpcClient rpcClient() {
       return new SolanaRpcClient(cluster());
    }

    @Bean
    public Cluster cluster() {
        return resolveCluster();
    }

    @Bean
    public Network network() {
        return resolveNetwork();
    }

    @Bean
    public WalletInfo wallet() {
        SolanaWallet wallet = new SolanaWallet(walletKey, null, network());
        return new WalletInfo(wallet, totalAccountsToInitialize);
    }

    private Cluster resolveCluster() {
        return switch (clusterName) {
            case "HELIUS" -> Cluster.HELIUS;
            case "MAINNET" -> Cluster.MAINNET;
            case "DEVNET" -> Cluster.DEVNET;
            case "TESTNET" -> Cluster.TESTNET;
            default -> Cluster.HELIUS;
        };
    }

    private Network resolveNetwork() {
        return switch (networkName) {
            case "TESTNET" -> Network.TESTNET;
            default -> Network.MAINNET;
        };
    }
}
