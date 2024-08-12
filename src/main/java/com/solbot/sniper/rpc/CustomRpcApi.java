package com.solbot.sniper.rpc;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomRpcApi {
    private final SolanaRpcClient rpcClient;

    public CustomRpcApi(SolanaRpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public AccountInfo getAccountInfo(String account, Map<String, Object> additionalParams) throws RpcException, com.paymennt.solanaj.api.rpc.RpcException {
        List<Object> params = new ArrayList<>();
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("encoding", additionalParams.getOrDefault("encoding", "base64"));
        if (additionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment)additionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }

        if (additionalParams.containsKey("dataSlice")) {
            parameterMap.put("dataSlice", additionalParams.get("dataSlice"));
        }

        if (additionalParams.containsKey("minContextSlot")) {
            parameterMap.put("minContextSlot", additionalParams.get("minContextSlot"));
        }

        params.add(account);
        params.add(parameterMap);
        return this.rpcClient.callForSerumMarketAccount("getAccountInfo", params, AccountInfo.class);
    }
}
