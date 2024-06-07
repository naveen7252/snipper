package com.solbot.sniper.service;

import com.solbot.sniper.data.TokenHolderInfo;
import com.solbot.sniper.data.TokenMint;

import java.util.Optional;

public interface TokenManagementService {
    Optional<TokenMint> getTokenMintInfo(String mint);

    TokenHolderInfo getTokenHolderInfo(String mintAddress);

    long getTokenAccountBalance(String tokenAccount);
}
