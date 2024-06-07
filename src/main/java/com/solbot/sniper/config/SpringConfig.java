package com.solbot.sniper.config;

import com.paymennt.crypto.bip32.Network;
import com.paymennt.solanaj.api.rpc.RpcEndPoint;
import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.ws.SolanaWebSocketClient;
import com.paymennt.solanaj.utils.WebsocketClient;
import com.paymennt.solanaj.wallet.SolanaWallet;
import com.solbot.sniper.data.WalletInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Value("${solana.network}")
    private String networkName;

    @Value("${walletKey}")
    private String walletKey;

    @Value("${solana.total.accounts.initialize}")
    private int totalAccountsToInitialize;

    @Value("${endPoint}")
    private String endPoint;

    @Bean
    public SolanaRpcClient rpcClient() {
       return new SolanaRpcClient(rpcEndPoint());
    }

    @Bean
    public RpcEndPoint rpcEndPoint() {
        return new RpcEndPoint(endPoint);
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

    @Bean
    public SolanaWebSocketClient webSocketClient() {
        return new SolanaWebSocketClient(rpcEndPoint());
    }

    private Network resolveNetwork() {
        return switch (networkName) {
            case "TESTNET" -> Network.TESTNET;
            default -> Network.MAINNET;
        };
    }
}
