package com.cosmocats.market.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.cosmocats.market.dto.ExchangeRateDto;

/** HTTP client of the external service that provides the cosmic currency exchange rate. */
@Component
public class CosmicExchangeClient {

    private static final String EXCHANGE_RATES_PATH = "/api/exchange-rates";

    private final RestClient restClient;

    public CosmicExchangeClient(RestClient.Builder restClientBuilder,
            @Value("${cosmic-exchange.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public double getExchangeRate() {
        return Optional.ofNullable(restClient.get()
                        .uri(EXCHANGE_RATES_PATH)
                        .retrieve()
                        .body(ExchangeRateDto.class))
                .map(ExchangeRateDto::getRate)
                .orElseThrow(() -> new IllegalStateException("Exchange service returned an empty response"));
    }
}
