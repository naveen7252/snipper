package com.solbot.sniper.service;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;
import com.paymennt.solanaj.api.ws.SolanaWebSocketClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static com.solbot.sniper.constant.Constants.RAYDIUM_PROGRAM_ID;

@Service
public class SolanaEventSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(SolanaEventSubscriber.class);
    private final SolanaWebSocketClient webSocketClient;
    private final SolanaRpcClient rpcClient;
    private final ScheduledExecutorService lpExecutorService;
    private final PositionService positionService;

    @Autowired
    public SolanaEventSubscriber(SolanaWebSocketClient webSocketClient,
                                 SolanaRpcClient rpcClient,
                                 PositionService positionService,
                                 ScheduledExecutorService lpExecutorService) {
        this.webSocketClient = webSocketClient;
        this.rpcClient = rpcClient;
        this.positionService = positionService;
        this.lpExecutorService = lpExecutorService;
    }

    @PostConstruct
    public void init() {
        subscribeToLogs(RAYDIUM_PROGRAM_ID);
    }

    public void subscribeToLogs(String key) {
        LOG.info("Subscribed to new LPs [ProgramId={}]", RAYDIUM_PROGRAM_ID);
        webSocketClient.logsSubscribe(key, SolanaCommitment.confirmed, logData -> {
            if (logData.get("err") == null) {
                List<String> logs = (List<String>) logData.get("logs");
                Optional<String> createLPLog = logs.stream().filter(log -> log.contains("init_pc_amount") || log.contains("initialize2")).findFirst();
                createLPLog.ifPresent(lpLog -> lpExecutorService.execute(() -> {
                    String signature = (String) logData.get("signature");
                    positionService.startPosition(signature, lpLog);
                }));
            }
        });
    }
}
