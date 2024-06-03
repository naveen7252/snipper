package com.solbot.sniper.service;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.LatestBlockHash;
import com.paymennt.solanaj.api.rpc.types.RpcResponse;
import com.paymennt.solanaj.api.rpc.types.SolanaCommitment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class UtilityService {
    private static final Logger LOG = LoggerFactory.getLogger(UtilityService.class);

    private UtilityService() {
    }

    public static boolean isBlockHashValid(SolanaRpcClient client, String blockHash, SolanaCommitment solanaCommitment) {
        RpcResponse<Object> rpcResponse = client.getApi().isBlockHashValidWithoutMapping(blockHash, solanaCommitment);
        LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) rpcResponse.getResult();
        return (boolean) result.get("value");
    }

    public static LatestBlockHash getLatestBlockHash(SolanaRpcClient client, SolanaCommitment solanaCommitment) {
        final LatestBlockHash latestBlockHash = client.getApi().getLatestBlockHash(solanaCommitment);
        LOG.info("recentBlockHash [{}]", latestBlockHash);
        return latestBlockHash;
    }
}
