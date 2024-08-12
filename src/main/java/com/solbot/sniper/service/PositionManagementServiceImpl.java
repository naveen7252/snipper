package com.solbot.sniper.service;

import com.mmorrell.serum.model.Market;
import com.mmorrell.serum.model.SerumUtils;
import com.paymennt.solanaj.api.rpc.SolanaRpcApi;
import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.RpcResponse;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;
import com.solbot.sniper.constant.SwapSide;
import com.solbot.sniper.constant.TransactionType;
import com.solbot.sniper.data.LpKeysInfo;
import com.solbot.sniper.data.SwapResult;
import com.solbot.sniper.data.TokenMint;
import com.solbot.sniper.data.TxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.solbot.sniper.constant.Constants.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
public class PositionManagementServiceImpl implements PositionManagementService {
    private static final Logger LOG = LoggerFactory.getLogger(PositionManagementServiceImpl.class);
    private final SolanaRpcApi rpcApi;
    private final TokenManagementService tokenManagementService;
    private final SerumMarketBuilder marketBuilder;
    private final AmmSwapService ammSwapService;
    private final TransactionService transactionService;
    private final ExecutorService lpExecutorService;

    @Autowired
    public PositionManagementServiceImpl(SolanaRpcClient rpcClient,
                                         TokenManagementService tokenManagementService,
                                         SerumMarketBuilder marketBuilder,
                                         AmmSwapService ammSwapService,
                                         TransactionService transactionService,
                                         ExecutorService lpExecutorService) {
        this.rpcApi = rpcClient.getApi();
        this.tokenManagementService = tokenManagementService;
        this.marketBuilder = marketBuilder;
        this.ammSwapService = ammSwapService;
        this.transactionService = transactionService;
        this.lpExecutorService = lpExecutorService;
    }

    @Override
    public void startPosition(String lpTxSignature, String lpLog) {
        long startTimeForTx = System.nanoTime();
        RpcResponse<Object> rpcResponse = rpcApi.getTransactionWithNoTransformation(lpTxSignature, SolanaCommitment.confirmed);
        LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) rpcResponse.getResult();
        if (result == null) {
            LOG.error("couldn't find transaction with signature, retrying to retrieve one more time after 2 sec -> {} ", lpTxSignature);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rpcResponse = rpcApi.getTransactionWithNoTransformation(lpTxSignature, SolanaCommitment.confirmed);
            result = (LinkedHashMap<String, Object>) rpcResponse.getResult();
            if (result == null) {
                LOG.error("couldn't find transaction with signature --> {}", lpTxSignature);
                return;
            }
        }
        long timeTakenForTxRetrieval = System.nanoTime() - startTimeForTx;
        LOG.info("Time taken for transaction retrieval = {}", timeTakenForTxRetrieval / 1_000_000L);
        LinkedHashMap<String, Object> transactionMap = (LinkedHashMap<String, Object>) result.get("transaction");
        LinkedHashMap<String, Object> messageMap = (LinkedHashMap<String, Object>) transactionMap.get("message");
        List<LinkedHashMap<String, Object>> instructions = (List<LinkedHashMap<String, Object>>) messageMap.get("instructions");
        Optional<LinkedHashMap<String, Object>> newLpInstructionMap = instructions.stream().filter(instructionMap -> {
            String programId = (String) instructionMap.get("programId");
            return programId != null && programId.equals(RAYDIUM_PROGRAM_ID);
        }).findFirst();
        // Instructions map
        if (newLpInstructionMap.isPresent()) {
            List<String> accounts = (List<String>) newLpInstructionMap.get().get("accounts");
            String ammId = accounts.get(4);
            String baseMint = accounts.get(8);
            String baseVault = accounts.get(10);
            String quoteMint = accounts.get(9);
            String quoteVault = accounts.get(11);
            String lpMint = accounts.get(7);
            String authority = accounts.get(5);
            String openOrders = accounts.get(6);
            String targetOrders = accounts.get(13);
            String withdrawQueue = "11111111111111111111111111111111";
            int marketVersion = 3;
            String marketProgramId = accounts.get(15);
            String marketId = accounts.get(16);
            if (!baseMint.equals(SOL_MINT) && !quoteMint.equals(SOL_MINT)) {
                LOG.error("Non native quote mint..not proceeding");
                return;
            }
            boolean baseAndQuoteSwapped = baseMint.equals(SOL_MINT);
            if (baseAndQuoteSwapped) {
                LOG.error("base and quote mint are swapped, not proceeding for now to avoid complex logic"); //TODO revisit later
                return;
            }
            int lpDecimals = SOL_DECIMALS;
            int baseDecimals = SOL_DECIMALS;
            int quoteDecimals = SOL_DECIMALS;
            String lpVault = null;
            BigInteger baseReserve = BigInteger.ZERO;
            BigInteger quoteReserve = BigInteger.ZERO;
            BigInteger lpReserve = BigInteger.ZERO;
            long openingTime = 0L;
            boolean isLpBurned = false;
            double lpBurnPercentage = 0;
            LinkedHashMap<String, Object> initializeMintInstruction = null;
            LinkedHashMap<String, Object> lpMintInstruction = null;
            LinkedHashMap<String, Object> baseTransferInstruction = null;
            LinkedHashMap<String, Object> quoteTransferInstruction = null;


            // Meta.innerInstructions
            LinkedHashMap<String, Object> metaMap = (LinkedHashMap<String, Object>) result.get("meta");
            List<LinkedHashMap<String, Object>> innerInstructions = (List<LinkedHashMap<String, Object>>) metaMap.get("innerInstructions");
            for (LinkedHashMap<String, Object> innerInstruction : innerInstructions) {
                List<LinkedHashMap<String, Object>> instructionList = (List<LinkedHashMap<String, Object>>) innerInstruction.get("instructions");
                for (LinkedHashMap<String, Object> inst : instructionList) {
                    LinkedHashMap<String, Object> parsed = (LinkedHashMap<String, Object>) inst.get("parsed");
                    if (parsed == null) {
                        continue;
                    }
                    String type = (String) parsed.get("type");
                    LinkedHashMap<String, Object> info = (LinkedHashMap<String, Object>) parsed.get("info");
                    if (type.equals(INITIALIZE_MINT)) {
                        String mint = (String) info.get("mint");
                        if (mint.equals(lpMint)) {
                            initializeMintInstruction = inst;
                            lpDecimals = (int) info.get("decimals");
                        }
                    }
                    if (type.equals("mintTo")) {
                        String mint = (String) info.get("mint");
                        if (mint.equals(lpMint)) {
                            lpMintInstruction = inst;
                            lpVault = (String) info.get("account");
                            lpReserve = new BigInteger((String) info.get("amount"));
                        }
                    }
                    String programId = (String) inst.get("programId");
                    if (type.equals("transfer") && programId.equals(TOKEN_PROGRAM_ID)) {
                        String destination = (String) info.get("destination");
                        if (destination.equals(baseVault)) {
                            baseReserve = new BigInteger((String) info.get("amount"));
                            baseTransferInstruction = inst;
                        } else if (destination.equals(quoteVault)) {
                            quoteReserve = new BigInteger((String) info.get("amount"));
                            quoteTransferInstruction = inst;
                        }
                    }
                }
            }
            Optional<String> openTimeLog = Arrays.stream(lpLog.split(",")).filter(str -> str.trim().startsWith("open_time")).findFirst();
            if (openTimeLog.isPresent()) {
                String openTime = openTimeLog.get().split(":")[1].trim();
                openingTime = Long.parseLong(openTime);
            }
            List<LinkedHashMap<String, Object>> pretokenBalances = (List<LinkedHashMap<String, Object>>) metaMap.get("preTokenBalances");
            Optional<LinkedHashMap<String, Object>> basePreBalanceOpt = pretokenBalances.stream().filter(preToken -> {
                String programId = (String) preToken.get("programId");
                return programId.equals(baseMint);
            }).findFirst();
            if (basePreBalanceOpt.isPresent()) {
                LinkedHashMap<String, Object> basePreBalance = basePreBalanceOpt.get();
                LinkedHashMap<String, Object> uiTokenAmountMap = (LinkedHashMap<String, Object>) basePreBalance.get("uiTokenAmount");
                int decimals = (int) uiTokenAmountMap.get("decimals");
                baseDecimals = baseAndQuoteSwapped ? SOL_DECIMALS : decimals;
                quoteDecimals = baseAndQuoteSwapped ? decimals : SOL_DECIMALS;
            }

            for (LinkedHashMap<String, Object> instruction : instructions) {
                LinkedHashMap<String, Object> parsed = (LinkedHashMap<String, Object>) instruction.get("parsed");
                if (parsed == null) {
                    continue;
                }
                String type = (String) parsed.get("type");
                if (type != null && type.equals("burnChecked")) {
                    LinkedHashMap<String, Object> info = (LinkedHashMap<String, Object>) parsed.get("info");
                    if (info == null) {
                        continue;
                    }
                    String burnFromAccount = (String) info.get("account");
                    String burnMint = (String) info.get("mint");
                    if ((burnFromAccount != null && burnFromAccount.equals(lpVault)) && (burnMint != null && burnMint.equals(lpMint))) {
                        LinkedHashMap<String, Object> tokenAmount = (LinkedHashMap<String, Object>) info.get("tokenAmount");
                        BigInteger burnAmount = new BigInteger((String) tokenAmount.get("amount"));
                        isLpBurned = burnAmount.longValue() > 0;
                        lpBurnPercentage = ((double) burnAmount.longValue() / lpReserve.longValue()) * 100;
                    }
                }
            }
            final LpKeysInfo lpKeysInfo = new LpKeysInfo(ammId,
                    baseAndQuoteSwapped ? quoteMint : baseMint,
                    baseAndQuoteSwapped ? baseMint : quoteMint,
                    lpMint,
                    baseDecimals,
                    quoteDecimals,
                    lpDecimals,
                    RAYDIUM_PROGRAM_ID,
                    authority,
                    baseVault,
                    quoteVault,
                    lpVault,
                    baseReserve,
                    quoteReserve,
                    lpReserve,
                    openingTime,
                    isLpBurned,
                    lpBurnPercentage);
            lpKeysInfo.setTxSignature(lpTxSignature);
            lpKeysInfo.setMarketId(marketId);
            lpKeysInfo.setMarketProgramId(marketProgramId);

            lpKeysInfo.setOpenOrders(openOrders);
            lpKeysInfo.setTargetOrders(targetOrders);
            lpKeysInfo.setWithdrawQueue(withdrawQueue);
            lpKeysInfo.setMarketVersion(marketVersion);

            // get market info
            LOG.info("Get market info for marketId = {} ", marketId);
            Market serumMarket = marketBuilder.build(marketId);
            lpKeysInfo.setMarketBaseVault(serumMarket.getBaseVault().toBase58());
            lpKeysInfo.setMarketQuoteVault(serumMarket.getQuoteVault().toBase58());
            lpKeysInfo.setMarketBids(serumMarket.getBids().toBase58());
            lpKeysInfo.setMarketAsks(serumMarket.getAsks().toBase58());
            lpKeysInfo.setMarketEventQueue(serumMarket.getEventQueueKey().toBase58());
            lpKeysInfo.setMarketVaultSigner(SerumUtils.getVaultSigner(serumMarket).toBase58());
            LOG.info("**************************************");
            LOG.info("*********** LP Keys Info *************");
            LOG.info("{}", lpKeysInfo);
            LOG.info("**************************************");
            final Optional<TokenMint> tokenMintOptional = tokenManagementService.getTokenMintInfo(lpKeysInfo.getBaseMint());
            if (tokenMintOptional.isEmpty()) {
                LOG.error("Couldn't get token mint info : {}", lpKeysInfo.getBaseMint());
                return;
            }
            TokenMint tokenMint = tokenMintOptional.get();
            if (!tokenMint.isRenounced()) {
                LOG.error("Not proceeding... mintAuthority/freezeAuthority is not renounced for mint :" + tokenMint.getMintAddress());
                return;
            }
            /*TokenHolderInfo holderInfo = tokenManagement.getTokenHolderInfo(lpKeysInfo.getBaseMint());
            System.out.println("Top 20 holders: "+ holderInfo.getTop20Holders());*/
            BigInteger supply = tokenMint.getSupply();
            double lpPercent = ((double) baseReserve.longValue() / supply.longValue()) * 100;
            LOG.info("***** Token Balance Info ******");
            LOG.info("Total Supply = {}, LP token supply = {}, lpPercent = {} ", supply, baseReserve, lpPercent);

            boolean isGoodLpBurn = isLpBurned && lpBurnPercentage > 90;
            if (isGoodLpBurn && lpPercent > 80) {
                LOG.info("$$$$$$$$$$$$$$ CAN APE IN $$$$$$$$$$$$$$$$$$$$$$ -> {} ", ammId);
            }
            long now = System.currentTimeMillis();
            long openTimeMs = openingTime * MILLIS_PER_SEC;
            LOG.info("Open Time={}, now={} , lpAvailable in millis={}", openTimeMs , now, openTimeMs - now);
            if (openingTime == 0 || openTimeMs <= now) {
                if (quoteReserve.longValue() >= 5 * ONE_SOL && quoteReserve.longValue() <= 100 * ONE_SOL && lpPercent > 50) {
                    LOG.info("Submitting swap as mint is renounced and Sol liquidity >= 5 ...");
                    SwapResult swapResult = ammSwapService.swap(lpKeysInfo, BigInteger.valueOf(3000000), BigInteger.ZERO, SwapSide.IN, TransactionType.BUY);
                    boolean isBuySwapConfirmed = false;
                    if (swapResult.getTxResult().isConfirmed()) {
                        LOG.info("Swap BUY confirmed [swapResult={}]", swapResult);
                        isBuySwapConfirmed = true;
                    } else {
                        LOG.info("Buy Swap is not confirmed, check again after few seconds..");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            LOG.error("Error = {}", e.getMessage(), e);
                        }
                        TxResult txResult = transactionService.getTransactionConfirmation(swapResult.getTxResult().getSignature());
                        if (txResult.isConfirmed()) {
                            LOG.info("Buy Swap is confirmed after recheck [TxResult={}]", txResult);
                            isBuySwapConfirmed = true;
                        }
                    }
                    if (isBuySwapConfirmed) {
                        LOG.info("Has bought, manage position now...");
                        try {
                            Thread.sleep(15000);// wait for 30 sec to sell
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LOG.info("Sell now...");
                        String baseTokenAccount = swapResult.getMintTokenAccount();
                        long tokenBalance = tokenManagementService.getTokenAccountBalance(baseTokenAccount);
                        SwapResult sellSwapResult = ammSwapService.swap(lpKeysInfo, BigInteger.valueOf(tokenBalance), BigInteger.ZERO, SwapSide.IN, TransactionType.SELL);
                        if (sellSwapResult.getTxResult().isConfirmed()) {
                            LOG.info("Sell tx confirmed [Tx={}]", sellSwapResult.getTxResult().getSignature());
                        } else {
                            LOG.info("Sell swap not confirmed, recheck again in few seconds...");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            TxResult sellTxResult = transactionService.getTransactionConfirmation(sellSwapResult.getTxResult().getSignature());
                            LOG.info("Sell tx status [{}]", sellTxResult.isConfirmed() ? "Confirmed" : "Not confirmed");
                        }
                    }
                }
            }
            System.out.println("<==============================> ");
        }
    }
}
