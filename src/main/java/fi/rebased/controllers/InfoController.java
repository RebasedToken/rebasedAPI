package fi.rebased.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import fi.rebased.coinGecko.Reb;
import fi.rebased.dto.McapRespDTO;
import fi.rebased.dto.TotalSupplyRespDTO;
import fi.rebased.etherscan.TokenSupply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController("/v1")
public class InfoController {
    private final long timeout = 10000;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    Environment env;

    private String getApiKey() {
        return env.getProperty("apiKey");
    }
    @Value("${rebContract}")
    private String rebContract;
    @Value("${totalSupplyUri}")
    private String totalSupplyUri;
    @Value("${priceUri}")
    private String priceUri;

    Supplier<TokenSupply> tokenSupplyCache = Suppliers.memoizeWithExpiration(this::fetchTokenSupply, 5, TimeUnit.SECONDS);
    Supplier<String> priceCache = Suppliers.memoizeWithExpiration(this::fetchPrice, 5, TimeUnit.SECONDS);

    @GetMapping("/totalsupply")
    TotalSupplyRespDTO totalSupply() {
        TokenSupply supply = tokenSupplyCache.get();
        TotalSupplyRespDTO totalSupplyRespDTO = new TotalSupplyRespDTO();
        if(supply.getStatus().equals("1")) {
            totalSupplyRespDTO.setTotalSupply(supply.getResult());
        }
        else {
            totalSupplyRespDTO.setError(supply.getResult());
        }
        return totalSupplyRespDTO;
    }

    private TokenSupply fetchTokenSupply() {
        String reqUri = String.format(totalSupplyUri, rebContract, getApiKey());
        return restTemplate.getForObject(reqUri, TokenSupply.class);
    }

    @GetMapping("mcap")
    McapRespDTO mcap() {
        TokenSupply rebSupply = tokenSupplyCache.get();
        String price = priceCache.get();
        McapRespDTO mcapRespDTO = new McapRespDTO();
        if(rebSupply.getStatus().equals("1") && price != null) {
            BigDecimal sup = new BigDecimal(rebSupply.getResult());
            BigDecimal rebPrice = new BigDecimal(price);
            mcapRespDTO.setMarketCap(sup.multiply(rebPrice));
        }
        else {
            if(price == null) {
                mcapRespDTO.setError("Invalid price from coingecko");
            }
            else {
                mcapRespDTO.setError(rebSupply.getResult());
            }
        }
        return mcapRespDTO;
    }

    private String fetchPrice() {
        String reqUri = String.format(priceUri, rebContract);
        String response = restTemplate.getForObject(reqUri, String.class);
        Map<String, Reb> map;
        try {
            map = mapper.readValue(response, new TypeReference<Map<String, Reb>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
        return map.get(rebContract.toLowerCase()).getUsd();
    }
}
