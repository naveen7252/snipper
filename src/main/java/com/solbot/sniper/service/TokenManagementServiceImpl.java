package com.solbot.sniper.service;

import com.paymennt.solanaj.api.rpc.SolanaRpcClient;
import com.paymennt.solanaj.api.rpc.types.RpcResponse;
import com.solbot.sniper.constant.CurrentSolanaCommitment;
import com.solbot.sniper.data.TokenHolderInfo;
import com.solbot.sniper.data.TokenMint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TokenManagementServiceImpl implements TokenManagementService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenManagementServiceImpl.class);
    private final SolanaRpcClient rpcClient;

    @Autowired
    public TokenManagementServiceImpl(SolanaRpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public Optional<TokenMint> getTokenMintInfo(String mint) {
        try {
            RpcResponse<Object> rpcResponse = rpcClient.getApi().getAccountInfoWithoutTypeMapping(mint, CurrentSolanaCommitment.getCommitment());
            LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) rpcResponse.getResult();
            if (result == null) {
                throw new RuntimeException("Couldn't find Token mint with given address : " + mint);
            }
            LinkedHashMap<String, Object> value = (LinkedHashMap<String, Object>) result.get("value");
            if (value == null) {
                return Optional.empty();
            }
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) value.get("data");
            if (data == null) {
                return Optional.empty();
            }
            LinkedHashMap<String, Object> parsed = (LinkedHashMap<String, Object>) data.get("parsed");
            if (parsed == null) {
                return Optional.empty();
            }
            String type = (String) parsed.get("type");
            if (type == null || !type.equals("mint")) {
                return Optional.empty();
            }
            LinkedHashMap<String, Object> info = (LinkedHashMap<String, Object>) parsed.get("info");
            if (info == null) {
                return Optional.empty();
            }
            int decimals = (int) info.get("decimals");
            String freezeAuthority = (String) info.get("freezeAuthority");
            String mintAuthority = (String) info.get("mintAuthority");
            BigInteger supply = new BigInteger((String) info.get("supply"));
            boolean isInitialized = (boolean) info.get("isInitialized");
            return Optional.of(new TokenMint(mint, decimals, freezeAuthority, mintAuthority, supply, isInitialized));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public TokenHolderInfo getTokenHolderInfo(String mintAddress) {

        final TokenHolderInfo holderInfo = new TokenHolderInfo(mintAddress);
        LOG.info("Getting holder info for the mint: " + mintAddress);
        RpcResponse<Object> rpcResponse = rpcClient.getApi().getTokenLargestAccounts(mintAddress, CurrentSolanaCommitment.getCommitment());
        LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) rpcResponse.getResult();
        List<LinkedHashMap<String, Object>> values = (List<LinkedHashMap<String, Object>>) result.get("value");
        for (LinkedHashMap<String, Object> data : values) {
            String tokenAcc = (String) data.get("address");
            BigDecimal balance = new BigDecimal((String) data.get("uiAmountString"));
            holderInfo.setTokenBalance(tokenAcc, balance);
        }
        return holderInfo;
    }

    @Override
    public long getTokenAccountBalance(String tokenAccount) {
        return rpcClient.getApi().getTokenAccountBalance(tokenAccount, CurrentSolanaCommitment.getCommitment());
    }
}
