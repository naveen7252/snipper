package com.solbot.sniper.data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TokenHolderInfo {

    private final String tokenMint;
    private final Map<String, BigDecimal> top20Holders = new LinkedHashMap<>(20);
    private BigDecimal creatorBalance;
    private BigDecimal lpReserveBalance;
    private double creatorPercent;
    private double lpReservePercent;
    private double top20HoldersPercent;
    private double top10HoldersPercent;
    private double top5HoldersPercent;

    public TokenHolderInfo(String tokenMint) {
        this.tokenMint = tokenMint;
    }

    public String getTokenMint() {
        return tokenMint;
    }

    public Map<String, BigDecimal> getTop20Holders() {
        return Collections.unmodifiableMap(top20Holders);
    }

    public void setTokenBalance(String account, BigDecimal amount){
        top20Holders.put(account, amount);
    }

    public BigDecimal getCreatorBalance() {
        return creatorBalance;
    }

    public void setCreatorBalance(BigDecimal creatorBalance) {
        this.creatorBalance = creatorBalance;
    }

    public BigDecimal getLpReserveBalance() {
        return lpReserveBalance;
    }

    public void setLpReserveBalance(BigDecimal lpReserveBalance) {
        this.lpReserveBalance = lpReserveBalance;
    }

    public double getCreatorPercent() {
        return creatorPercent;
    }

    public void setCreatorPercent(double creatorPercent) {
        this.creatorPercent = creatorPercent;
    }

    public double getLpReservePercent() {
        return lpReservePercent;
    }

    public void setLpReservePercent(double lpReservePercent) {
        this.lpReservePercent = lpReservePercent;
    }

    public double getTop20HoldersPercent() {
        return top20HoldersPercent;
    }

    public void setTop20HoldersPercent(double top20HoldersPercent) {
        this.top20HoldersPercent = top20HoldersPercent;
    }

    public double getTop10HoldersPercent() {
        return top10HoldersPercent;
    }

    public void setTop10HoldersPercent(double top10HoldersPercent) {
        this.top10HoldersPercent = top10HoldersPercent;
    }

    public double getTop5HoldersPercent() {
        return top5HoldersPercent;
    }

    public void setTop5HoldersPercent(double top5HoldersPercent) {
        this.top5HoldersPercent = top5HoldersPercent;
    }

    @Override
    public String toString() {
        return "TokenHolderInfo{" +
                "tokenMint='" + tokenMint + '\'' +
                ", top20Holders=" + top20Holders +
                ", creatorBalance=" + creatorBalance +
                ", lpReserveBalance=" + lpReserveBalance +
                ", creatorHoldingPercent=" + creatorPercent +
                ", lpReservePercent=" + lpReservePercent +
                ", top20HoldersPercent=" + top20HoldersPercent +
                ", top10HoldersPercent=" + top10HoldersPercent +
                ", top5HoldersPercent=" + top5HoldersPercent +
                '}';
    }
}
