package com.solbot.sniper.data;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;

import static com.solbot.sniper.constant.Constants.ONE_SOL;

public class LpKeysInfo {

    private final String ammId;
    private String txSignature;
    private final String baseMint;
    private final String quoteMint;
    private final String lpMint;
    private final int baseDecimals;
    private final int quoteDecimals;
    private final int lpDecimals;
    private final int version = 4;
    private final String programId;

    private final String authority;
    private final String baseVault;
    private final String quoteVault;
    private final String lpVault;
    private final BigInteger baseReserve;
    private final BigInteger quoteReserve;
    private final BigInteger lpReserve;
    private final long openTime;
    private final boolean isBurned;
    private final double burnPercentage;

    private String openOrders;
    private String targetOrders;
    private String withdrawQueue;
    private int marketVersion;
    private String marketProgramId;
    private String marketId;
    private String marketBaseVault;
    private String marketQuoteVault;
    private String marketBids;
    private String marketAsks;
    private String marketEventQueue;
    private String marketVaultSigner;


    public LpKeysInfo(String ammId,
                      String baseMint,
                      String quoteMint,
                      String lpMint,
                      int baseDecimals,
                      int quoteDecimals,
                      int lpDecimals,
                      String programId,
                      String authority,
                      String baseVault,
                      String quoteVault,
                      String lpVault,
                      BigInteger baseReserve,
                      BigInteger quoteReserve,
                      BigInteger lpReserve,
                      long openTime,
                      boolean isBurned,
                      double burnPercentage) {
        this.ammId = ammId;
        this.baseMint = baseMint;
        this.quoteMint = quoteMint;
        this.lpMint = lpMint;
        this.baseDecimals = baseDecimals;
        this.quoteDecimals = quoteDecimals;
        this.lpDecimals = lpDecimals;
        this.programId = programId;
        this.authority = authority;
        this.baseVault = baseVault;
        this.quoteVault = quoteVault;
        this.lpVault = lpVault;
        this.baseReserve = baseReserve;
        this.quoteReserve = quoteReserve;
        this.lpReserve = lpReserve;
        this.openTime = openTime;
        this.isBurned = isBurned;
        this.burnPercentage = burnPercentage;
    }

    public String getAmmId() {
        return ammId;
    }

    public String getBaseMint() {
        return baseMint;
    }

    public String getQuoteMint() {
        return quoteMint;
    }

    public String getLpMint() {
        return lpMint;
    }

    public int getBaseDecimals() {
        return baseDecimals;
    }

    public int getQuoteDecimals() {
        return quoteDecimals;
    }

    public int getLpDecimals() {
        return lpDecimals;
    }

    public int getVersion() {
        return version;
    }

    public String getProgramId() {
        return programId;
    }

    public String getAuthority() {
        return authority;
    }

    public String getBaseVault() {
        return baseVault;
    }

    public String getQuoteVault() {
        return quoteVault;
    }

    public String getLpVault() {
        return lpVault;
    }

    public BigInteger getBaseReserve() {
        return baseReserve;
    }

    public BigInteger getQuoteReserve() {
        return quoteReserve;
    }

    public BigInteger getLpReserve() {
        return lpReserve;
    }

    public long getOpenTime() {
        return openTime;
    }

    public boolean isBurned() {
        return isBurned;
    }

    public double getBurnPercentage() {
        return burnPercentage;
    }

    public String getOpenOrders() {
        return openOrders;
    }

    public void setOpenOrders(String openOrders) {
        this.openOrders = openOrders;
    }

    public String getTargetOrders() {
        return targetOrders;
    }

    public void setTargetOrders(String targetOrders) {
        this.targetOrders = targetOrders;
    }

    public String getWithdrawQueue() {
        return withdrawQueue;
    }

    public void setWithdrawQueue(String withdrawQueue) {
        this.withdrawQueue = withdrawQueue;
    }

    public int getMarketVersion() {
        return marketVersion;
    }

    public void setMarketVersion(int marketVersion) {
        this.marketVersion = marketVersion;
    }

    public String getMarketProgramId() {
        return marketProgramId;
    }

    public void setMarketProgramId(String marketProgramId) {
        this.marketProgramId = marketProgramId;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getMarketBaseVault() {
        return marketBaseVault;
    }

    public void setMarketBaseVault(String marketBaseVault) {
        this.marketBaseVault = marketBaseVault;
    }

    public String getMarketQuoteVault() {
        return marketQuoteVault;
    }

    public void setMarketQuoteVault(String marketQuoteVault) {
        this.marketQuoteVault = marketQuoteVault;
    }

    public String getMarketBids() {
        return marketBids;
    }

    public void setMarketBids(String marketBids) {
        this.marketBids = marketBids;
    }

    public String getMarketAsks() {
        return marketAsks;
    }

    public void setMarketAsks(String marketAsks) {
        this.marketAsks = marketAsks;
    }

    public String getMarketEventQueue() {
        return marketEventQueue;
    }

    public void setMarketEventQueue(String marketEventQueue) {
        this.marketEventQueue = marketEventQueue;
    }

    public String getMarketVaultSigner() {
        return marketVaultSigner;
    }

    public void setMarketVaultSigner(String marketVaultSigner) {
        this.marketVaultSigner = marketVaultSigner;
    }

    public String getTxSignature() {
        return txSignature;
    }

    public void setTxSignature(String txSignature) {
        this.txSignature = txSignature;
    }

    @Override
    public String toString() {
        return "LpKeysInfo{" +
                " ammId='" + ammId + '\n' +
                " txSignature='" + txSignature + '\n' +
                ", baseMint='" + baseMint + '\n' +
                ", quoteMint='" + quoteMint + '\n' +
                ", lpMint='" + lpMint + '\n' +
                ", baseDecimals=" + baseDecimals +'\n' +
                ", quoteDecimals=" + quoteDecimals +'\n' +
                ", lpDecimals=" + lpDecimals +'\n' +
                ", version=" + version +'\n' +
                ", programId='" + programId + '\'' +'\n' +
                ", authority='" + authority + '\'' +'\n' +
                ", baseVault='" + baseVault + '\'' +'\n' +
                ", quoteVault='" + quoteVault + '\'' +'\n' +
                ", lpVault='" + lpVault + '\'' +'\n' +
                ", baseReserve=" + (double) baseReserve.longValue() / ONE_SOL +'\n' +
                ", quoteReserve=" + (double) quoteReserve.longValue() / ONE_SOL +'\n' +
                ", lpReserve=" + lpReserve +'\n' +
                ", openTime=" + openTime +'\n' +
                ", isBurned=" + isBurned +'\n' +
                ", burnPercentage=" + burnPercentage +'\n' +
                ", openOrders='" + openOrders + '\'' +'\n' +
                ", targetOrders='" + targetOrders + '\'' +'\n' +
                ", withdrawQueue='" + withdrawQueue + '\'' +'\n' +
                ", marketVersion=" + marketVersion +'\n' +
                ", marketProgramId='" + marketProgramId + '\'' +'\n' +
                ", marketId='" + marketId + '\'' +'\n' +
                ", marketBaseVault='" + marketBaseVault + '\'' +'\n' +
                ", marketQuoteVault='" + marketQuoteVault + '\'' +'\n' +
                ", marketBids='" + marketBids + '\'' +'\n' +
                ", marketAsks='" + marketAsks + '\'' +'\n' +
                ", marketEventQueue='" + marketEventQueue + '\'' +
                ", marketVaultSigner='" + marketVaultSigner + '\'' +
                '}';
    }

    private String millisToDateString(long millis) {
        Instant instance = Instant.ofEpochMilli(millis);
        LocalDateTime localDateTime = LocalDateTime
                .ofInstant(instance, java.time.ZoneId.systemDefault());
        return localDateTime.toString();
    }
}

