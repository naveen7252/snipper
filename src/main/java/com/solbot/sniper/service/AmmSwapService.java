package com.solbot.sniper.service;

import com.solbot.sniper.constant.SwapSide;
import com.solbot.sniper.constant.TransactionType;
import com.solbot.sniper.data.LpKeysInfo;
import com.solbot.sniper.data.SwapResult;

import java.math.BigInteger;

public interface AmmSwapService {
    SwapResult swap(LpKeysInfo lpKeysInfo, BigInteger amountIn, BigInteger amountOut, SwapSide swapSide, TransactionType transactionType);
}
