package com.solbot.sniper.service;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.LatestBlockHash;
import com.paymennt.solanaj.api.rpc.types.PriorityFeeType;
import com.paymennt.solanaj.data.*;
import com.paymennt.solanaj.program.ComputeBudgetProgram;
import com.paymennt.solanaj.program.SystemProgram;
import com.paymennt.solanaj.program.TokenProgram;
import com.solbot.sniper.constant.Constants;
import com.solbot.sniper.constant.CurrentSolanaCommitment;
import com.solbot.sniper.constant.SwapSide;
import com.solbot.sniper.constant.TransactionType;
import com.solbot.sniper.data.LpKeysInfo;
import com.solbot.sniper.data.SwapResult;
import com.solbot.sniper.data.TransactionParams;
import com.solbot.sniper.data.TxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Service
public class AmmSwapServiceImpl implements AmmSwapService {
    private static final Logger LOG = LoggerFactory.getLogger(AmmSwapServiceImpl.class);

    private final SolanaRpcClient rpcClient;
    private final WalletService walletService;
    private final TransactionService transactionService;

    public AmmSwapServiceImpl(SolanaRpcClient rpcClient,
                              WalletService walletService,
                              TransactionService transactionService) {
        this.rpcClient = rpcClient;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @Override
    public SwapResult swap(LpKeysInfo lpKeysInfo, BigInteger amountIn, BigInteger amountOut, SwapSide swapSide, TransactionType transactionType) {
        final String baseMint = lpKeysInfo.getBaseMint();
        final String quoteMint = lpKeysInfo.getQuoteMint();
        final SolanaAccount ownerAccount = walletService.getAccount(Constants.WALLET_ONE);
        final SolanaPublicKey owner = ownerAccount.getPublicKey();

        List<AccountMeta> accountKeys = new ArrayList<>();
        // system
        accountKeys.add(new AccountMeta(new SolanaPublicKey(Constants.TOKEN_PROGRAM_ID),false,false));

        //amm
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getAmmId()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getAuthority()),false,false));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getOpenOrders()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getTargetOrders()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getBaseVault()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getQuoteVault()),false,true));

        //serum
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketProgramId()),false,false));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketId()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketBids()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketAsks()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketEventQueue()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketBaseVault()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketQuoteVault()),false,true));
        accountKeys.add(new AccountMeta(new SolanaPublicKey(lpKeysInfo.getMarketVaultSigner()),false,false));

        //user
        SolanaPublicKey baseMintTokenAccount = TokenProgram.generateTokenAccountAddress(new SolanaPublicKey(baseMint), owner);
        SolanaPublicKey quoteMintTokenAccount = TokenProgram.generateTokenAccountAddress(new SolanaPublicKey(quoteMint), owner);
        LOG.info("quoteMintTokenAccount = [{}]", quoteMintTokenAccount.toBase58());
        LOG.info("baseMintTokenAccount = [{}] ",baseMintTokenAccount.toBase58());

        if (transactionType == TransactionType.BUY) {
            accountKeys.add(new AccountMeta(quoteMintTokenAccount,false,true));
            accountKeys.add(new AccountMeta(baseMintTokenAccount,false,true));
        } else {
            accountKeys.add(new AccountMeta(baseMintTokenAccount,false,true));
            accountKeys.add(new AccountMeta(quoteMintTokenAccount,false,true));
        }
        accountKeys.add(new AccountMeta(owner,true,true));

        byte[] instructionData;
        if (swapSide == SwapSide.IN) {
            instructionData = makeSwapFixedInTransactionInstructionData(amountIn.longValue(),amountOut.longValue());
        } else {
            instructionData = makeSwapFixedOutTransactionInstructionData(amountIn.longValue(),amountOut.longValue());
        }
        SolanaTransactionInstruction swapInstruction = new SolanaTransactionInstruction(new SolanaPublicKey(Constants.RAYDIUM_PROGRAM_ID),accountKeys, instructionData);
        SolanaTransactionInstruction createBaseTokenAccountInstruction = TokenProgram.createAccount(owner,new SolanaPublicKey(baseMint), owner);
        SolanaTransactionInstruction createQuoteTokenAccountInstruction = TokenProgram.createAccount(owner,new SolanaPublicKey(quoteMint), owner);


        //Compute budget
        SolanaTransactionInstruction computeUnitLimitInstruction = ComputeBudgetProgram.setComputeUnitLimit(65_000);//80_000
        EnumMap<PriorityFeeType, Integer> priorityFeeMap = rpcClient.getApi().getPriorityFee(List.of(Constants.RAYDIUM_PROGRAM_ID));
        int priorityFee = priorityFeeMap.getOrDefault(PriorityFeeType.VERY_HIGH, 0);
        priorityFee = priorityFee <= 10_000 ? 5_000_000 : priorityFee;
        SolanaTransactionInstruction computeUnitPriceInstruction = ComputeBudgetProgram.setComputeUnitPrice(5_000_000); //10_000_000

        //create transaction and add instructions
        SolanaTransaction transaction = new SolanaTransaction();
        transaction.addInstruction(computeUnitLimitInstruction);
        transaction.addInstruction(computeUnitPriceInstruction);

        if (transactionType == TransactionType.BUY) {
            //transaction.addInstruction(createQuoteTokenAccountInstruction);
            transaction.addInstruction(SystemProgram.transfer(owner, quoteMintTokenAccount, amountIn.longValue()));
            transaction.addInstruction(createSyncNativeInstruction(quoteMintTokenAccount));
            transaction.addInstruction(createBaseTokenAccountInstruction);
            transaction.addInstruction(swapInstruction);
        } else {
            transaction.addInstruction(swapInstruction);
            //close token account as selling whole amount
            SolanaTransactionInstruction closeBaseTokenAccountInstruction = TokenProgram.closeAccount(baseMintTokenAccount, owner, owner);
            transaction.addInstruction(closeBaseTokenAccountInstruction);
        }

        final LatestBlockHash latestBlockHash = rpcClient.getApi().getLatestBlockHash(CurrentSolanaCommitment.getCommitment());
        transaction.setRecentBlockHash(latestBlockHash.getLatestBlockHash());
        transaction.setFeePayer(owner);
        transaction.sign(ownerAccount);

        TxResult txResult = transactionService.submitTransaction(transaction, new TransactionParams(CurrentSolanaCommitment.getCommitment(),CurrentSolanaCommitment.getPreflightCommitment(),
                true, latestBlockHash, transactionType));
        if (txResult.isConfirmed()) {
            LOG.info("Transaction Confirmed: [{}]", txResult);
        } else {
            LOG.error("Transaction Not Confirmed: [{}]", txResult);
        }
        return new SwapResult(owner.toString(), baseMint, quoteMintTokenAccount.toString(), baseMintTokenAccount.toString(), txResult);
    }

    private SolanaTransactionInstruction createSyncNativeInstruction(SolanaPublicKey associatedToken) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(associatedToken, false, true));
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 17);
        return new SolanaTransactionInstruction(new SolanaPublicKey(Constants.TOKEN_PROGRAM_ID), keys, buffer.array());
    }

    private byte[] makeSwapFixedInTransactionInstructionData(long amountIn, long minAmountOut) {
        ByteBuffer result = ByteBuffer.allocate(17);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put( (byte) Constants.SWAP_FIXED_IN_INSTRUCTION);
        result.putLong(amountIn);
        result.putLong(minAmountOut);
        return result.array();
    }

    private byte[] makeSwapFixedOutTransactionInstructionData(long maxAmountIn, long amountOut) {
        ByteBuffer result = ByteBuffer.allocate(17);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put( (byte) Constants.SWAP_FIXED_OUT_INSTRUCTION);
        result.putLong(maxAmountIn);
        result.putLong(amountOut);
        return result.array();
    }
}
