package com.solbot.sniper.service;

import com.mmorrell.serum.model.Market;
import com.solbot.sniper.constant.Encoding;
import com.solbot.sniper.rpc.CustomRpcApi;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class SerumMarketBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SerumMarketBuilder.class);
    private final CustomRpcApi rpcApi;

    @Autowired
    public SerumMarketBuilder(CustomRpcApi rpcApi) {
        this.rpcApi = rpcApi;
    }

    public Market build(String marketId) {
        final byte[] base64AccountInfo = retrieveAccountDataConfirmed(marketId);
        return Market.readMarket(base64AccountInfo);
    }

    private byte[] retrieveAccountDataConfirmed(String publicKey) {
        AccountInfo orderBook;

        try {
            orderBook = rpcApi.getAccountInfo(
                    publicKey,
                    Map.of(
                            "commitment",
                            Commitment.CONFIRMED,
                            "encoding",
                            Encoding.base64
                    )
            );

            final List<String> accountData = orderBook.getValue().getData();
            return Base64.getDecoder().decode(accountData.get(0));
        } catch (RpcException e) {
            LOG.error("ERROR in retrieving serum market info: ",e);
        }

        return new byte[0];
    }
}
