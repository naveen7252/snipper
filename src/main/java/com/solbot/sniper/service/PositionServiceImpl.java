package com.solbot.sniper.service;

import com.mmorrell.serum.model.Market;
import com.mmorrell.serum.model.SerumUtils;
import com.paymennt.solanaj.api.rpc.SolanaRpcApi;
import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.RpcResponse;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;
import com.solbot.sniper.constant.SwapSide;
import com.solbot.sniper.constant.TransactionType;
import com.solbot.sniper.data.*;
import com.solbot.sniper.service.task.RetryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static com.solbot.sniper.constant.Constants.*;

@Service
public class PositionServiceImpl implements PositionService {
    private static final Logger LOG = LoggerFactory.getLogger(PositionServiceImpl.class);
    private static final String PROGRAM_ID = "programId";
    public static final String AMOUNT = "amount";
    private final SolanaRpcApi rpcApi;
    private final TokenManagementService tokenManagementService;
    private final SerumMarketBuilder marketBuilder;
    private final AmmService ammService;
    private final TransactionService transactionService;
    private final ScheduledExecutorService lpExecutorService;
    private final StrategyConfigs strategyConfigs;

    @Autowired
    public PositionServiceImpl(SolanaRpcClient rpcClient,
                               TokenManagementService tokenManagementService,
                               SerumMarketBuilder marketBuilder,
                               AmmService ammService,
                               TransactionService transactionService,
                               ScheduledExecutorService lpExecutorService,
                               StrategyConfigs strategyConfigs) {
        this.rpcApi = rpcClient.getApi();
        this.tokenManagementService = tokenManagementService;
        this.marketBuilder = marketBuilder;
        this.ammService = ammService;
        this.transactionService = transactionService;
        this.lpExecutorService = lpExecutorService;
        this.strategyConfigs = strategyConfigs;
        LOG.info("Strategy Configs [{}]", strategyConfigs);
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
                LOG.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
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
            String programId = (String) instructionMap.get(PROGRAM_ID);
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
                LOG.error("base and quote mint are swapped, not proceeding for now to avoid complex logic [baseMint={}, quoteMint={}]",
                        baseMint, quoteMint); //TODO revisit later
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
                            lpDecimals = (int) info.get("decimals");
                        }
                    }
                    if (type.equals("mintTo")) {
                        String mint = (String) info.get("mint");
                        if (mint.equals(lpMint)) {
                            lpVault = (String) info.get("account");
                            lpReserve = new BigInteger((String) info.get(AMOUNT));
                        }
                    }
                    String programId = (String) inst.get(PROGRAM_ID);
                    if (type.equals("transfer") && programId.equals(TOKEN_PROGRAM_ID)) {
                        String destination = (String) info.get("destination");
                        if (destination.equals(baseVault)) {
                            baseReserve = new BigInteger((String) info.get(AMOUNT));
                        } else if (destination.equals(quoteVault)) {
                            quoteReserve = new BigInteger((String) info.get(AMOUNT));
                        }
                    }
                }
            }
            Optional<String> openTimeLog = Arrays.stream(lpLog.split(",")).filter(str -> str.trim().startsWith("open_time")).findFirst();
            if (openTimeLog.isPresent()) {
                String openTime = openTimeLog.get().split(":")[1].trim();
                openingTime = Long.parseLong(openTime);
            }
            List<LinkedHashMap<String, Object>> preTokenBalances = (List<LinkedHashMap<String, Object>>) metaMap.get("preTokenBalances");
            Optional<LinkedHashMap<String, Object>> basePreBalanceOpt = preTokenBalances.stream().filter(preToken -> {
                String programId = (String) preToken.get(PROGRAM_ID);
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
                        BigInteger burnAmount = new BigInteger((String) tokenAmount.get(AMOUNT));
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
                LOG.error("Not proceeding... mintAuthority/freezeAuthority is not renounced [mint={}]", tokenMint.getMintAddress());
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
            LOG.info("Open Time={}, now={} , lpAvailable in millis={}", openTimeMs, now, openTimeMs - now);
            if (openingTime == 0 || openTimeMs <= now) {
                if (quoteReserve.longValue() >= strategyConfigs.getMinSolLiquidity()
                        && quoteReserve.longValue() <= strategyConfigs.getMaxSolLiquidity()
                        && lpPercent > strategyConfigs.getMinLpPercent()) {
                    LOG.info("Submitting swap as mint is renounced and Sol liquidity >= {} ...", quoteReserve.longValue());
                    SwapResult swapResult = ammService.swap(lpKeysInfo, BigInteger.valueOf(3000000), BigInteger.ZERO, SwapSide.IN, TransactionType.BUY);
                    if (!swapResult.getTxResult().isConfirmed()) {
                        RetryHelper<TxResult> retryHelper = new RetryHelper<>(lpExecutorService,
                                () -> transactionService.getTransactionConfirmation(swapResult.getTxResult().getSignature()),
                                strategyConfigs.getInitialBuyTxConfirmWaitTimeMillis(),
                                5);
                        TxResult txResult = retryHelper.retry();
                        if (!txResult.isConfirmed()) {
                            LOG.info("Initial BUY transaction is not confirmed after max retries [ammId={}, TxSignature={}]", ammId, lpKeysInfo.getTxSignature());
                            return;
                        }
                    }
                    if (swapResult.getTxResult().getConfirmationStatus().isError()) {
                        LOG.error("Initial BUY transaction is failed, not continuing [tx={}]", swapResult.getTxResult().getSignature());
                    }
                    LOG.info("BUY successful!! Entered the position [ammId={}, mint={}]", ammId, baseMint);

                    try {
                        Thread.sleep(15000);// wait for 30 sec to sell
                    } catch (InterruptedException e) {
                        LOG.error("Error = {}", e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                    LOG.info("Sell now...");
                    final String baseTokenAccount = swapResult.getMintTokenAccount();
                    long tokenBalance = tokenManagementService.getTokenAccountBalance(baseTokenAccount);
                    final SwapResult sellSwapResult = ammService.swap(lpKeysInfo, BigInteger.valueOf(tokenBalance), BigInteger.ZERO, SwapSide.IN, TransactionType.SELL);
                    if (!sellSwapResult.getTxResult().isConfirmed()) {
                        RetryHelper<TxResult> retryHelper = new RetryHelper<>(lpExecutorService,
                                () -> transactionService.getTransactionConfirmation(sellSwapResult.getTxResult().getSignature()),
                                strategyConfigs.getInitialSellTxConfirmWaitTimeMillis(),
                                5);
                       final TxResult txResult = retryHelper.retry();
                        if (!txResult.isConfirmed()) {
                            LOG.error("SELL transaction is not confirmed after max retries [ammId={}, TxSignature={}]", ammId, lpKeysInfo.getTxSignature());
                            return;
                        }
                    }
                }
            }
            LOG.info("<==============================> ");
        }
    }
}
