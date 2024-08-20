package com.solbot.sniper.data;

import org.springframework.stereotype.Component;

import static com.solbot.sniper.constant.Constants.LAMPORTS_PER_SOL;

@Component
public class StrategyConfigs {

    private int minSolLiquidity;

    private int maxSolLiquidity;

    private int minLpPercent;

    private int minBurnPercent;

    private int initialBuyTxConfirmWaitTimeMillis;

    private int initialSellTxConfirmWaitTimeMillis;

    public void setMinSolLiquidity(int minSolLiquidity) {
        this.minSolLiquidity = minSolLiquidity;
    }

    public void setMaxSolLiquidity(int maxSolLiquidity) {
        this.maxSolLiquidity = maxSolLiquidity;
    }

    public void setMinLpPercent(int minLpPercent) {
        this.minLpPercent = minLpPercent;
    }

    public void setMinBurnPercent(int minBurnPercent) {
        this.minBurnPercent = minBurnPercent;
    }

    public void setInitialBuyTxConfirmWaitTimeMillis(int initialBuyTxConfirmWaitTimeMillis) {
        this.initialBuyTxConfirmWaitTimeMillis = initialBuyTxConfirmWaitTimeMillis;
    }

    public void setInitialSellTxConfirmWaitTimeMillis(int initialSellTxConfirmWaitTimeMillis) {
        this.initialSellTxConfirmWaitTimeMillis = initialSellTxConfirmWaitTimeMillis;
    }

    public long getMinSolLiquidity() {
        return minSolLiquidity * LAMPORTS_PER_SOL;
    }

    public long getMaxSolLiquidity() {
        return maxSolLiquidity * LAMPORTS_PER_SOL;
    }

    public int getMinLpPercent() {
        return minLpPercent;
    }

    public int getMinBurnPercent() {
        return minBurnPercent;
    }

    public long getInitialBuyTxConfirmWaitTimeMillis() {
        return initialBuyTxConfirmWaitTimeMillis;
    }

    public long getInitialSellTxConfirmWaitTimeMillis() {
        return initialSellTxConfirmWaitTimeMillis;
    }

    @Override
    public String toString() {
        return "StrategyConfigs{" +
                "minSolLiquidity=" + minSolLiquidity +
                ", maxSolLiquidity=" + maxSolLiquidity +
                ", minLpPercent=" + minLpPercent +
                ", minBurnPercent=" + minBurnPercent +
                ", initialBuyTxConfirmWaitTimeMillis=" + initialBuyTxConfirmWaitTimeMillis +
                ", initialSellTxConfirmWaitTimeMillis=" + initialSellTxConfirmWaitTimeMillis +
                '}';
    }
}
