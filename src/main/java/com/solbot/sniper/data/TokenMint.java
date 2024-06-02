package com.solbot.sniper.data;

import java.math.BigInteger;
import java.util.Objects;

public class TokenMint {

    private final String mintAddress;
    private final int decimals;
    private final String freezeAuthority;
    private final String mintAuthority;
    private final BigInteger supply;
    private final boolean isInitialized;
    private final boolean isRenounced;

    public TokenMint(String mintAddress,
                     int decimals,
                     String freezeAuthority,
                     String mintAuthority,
                     BigInteger supply,
                     boolean isInitialized) {
        this.mintAddress = mintAddress;
        this.decimals = decimals;
        this.freezeAuthority = freezeAuthority;
        this.mintAuthority = mintAuthority;
        this.supply = supply;
        this.isInitialized = isInitialized;
        this.isRenounced = mintAuthority == null && freezeAuthority == null;
    }

    public String getMintAddress() {
        return mintAddress;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getFreezeAuthority() {
        return freezeAuthority;
    }

    public String getMintAuthority() {
        return mintAuthority;
    }

    public BigInteger getSupply() {
        return supply;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isRenounced() {
        return isRenounced;
    }

    @Override
    public String toString() {
        return "TokenMint{" +
                "mintAddress='" + mintAddress + '\'' +
                ", decimals=" + decimals +
                ", freezeAuthority='" + freezeAuthority + '\'' +
                ", mintAuthority='" + mintAuthority + '\'' +
                ", supply=" + supply +
                ", isInitialized=" + isInitialized +
                ", isRenounced=" + isRenounced +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenMint tokenMint = (TokenMint) o;
        return mintAddress.equals(tokenMint.mintAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mintAddress);
    }
}
