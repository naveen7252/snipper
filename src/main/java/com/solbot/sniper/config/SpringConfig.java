package com.solbot.sniper.config;

import com.paymennt.crypto.bip32.Network;
import com.paymennt.solanaj.api.rpc.RpcEndPoint;
import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.ws.SolanaWebSocketClient;
import com.paymennt.solanaj.wallet.SolanaWallet;
import com.solbot.sniper.data.StrategyConfigs;
import com.solbot.sniper.data.WalletInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.solbot.sniper.constant.Constants.THREAD_POOL_SIZE;

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

    @Value("${solana.priority.fee.retrieve}")
    private boolean priorityFeeRetrieval;

    @Value("${solana.min.sol.liquidity}")
    private int minSolLiquidity;

    @Value("${solana.max.sol.liquidity}")
    private int maxSolLiquidity;

    @Value("${solana.min.lp.percentage}")
    private int minLpPercent;

    @Value("${solana.min.burn.percentage}")
    private int minBurnPercent;

    @Value("${solana.tx.initial.buy.check.wait}")
    private int initialBuyTxConfirmWaitTimeMillis;

    @Value("${solana.tx.initial.sell.check.wait}")
    private int initialSellTxConfirmWaitTimeMillis;

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
    public boolean getPriorityFeeFromApi() {
        return priorityFeeRetrieval;
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

    @Bean
    public ScheduledExecutorService lpExecutorService() {
        return Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    }

    @Bean
    public StrategyConfigs strategyConfigs() {
        StrategyConfigs strategyConfigs = new StrategyConfigs();
        strategyConfigs.setInitialSellTxConfirmWaitTimeMillis(initialSellTxConfirmWaitTimeMillis);
        strategyConfigs.setInitialBuyTxConfirmWaitTimeMillis(initialBuyTxConfirmWaitTimeMillis);
        strategyConfigs.setMinSolLiquidity(minSolLiquidity);
        strategyConfigs.setMaxSolLiquidity(maxSolLiquidity);
        strategyConfigs.setMinBurnPercent(minBurnPercent);
        strategyConfigs.setMinLpPercent(minLpPercent);
        return strategyConfigs;
    }

    private Network resolveNetwork() {
        return switch (networkName) {
            case "TESTNET" -> Network.TESTNET;
            default -> Network.MAINNET;
        };
    }
}
