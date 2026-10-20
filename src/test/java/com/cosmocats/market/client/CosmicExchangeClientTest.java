package com.cosmocats.market.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/** Partial-context integration test: only the client and a RestClient.Builder are created. */
@SpringJUnitConfig(CosmicExchangeClientTest.TestConfig.class)
class CosmicExchangeClientTest {

    private static final String EXCHANGE_RATES_URL = "/api/exchange-rates";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void registerWireMockUrl(DynamicPropertyRegistry registry) {
        registry.add("cosmic-exchange.base-url", wireMock::baseUrl);
    }

    @Autowired
    private CosmicExchangeClient cosmicExchangeClient;

    @Test
    void shouldReturnExchangeRateWhenServiceRespondsWithJson() {
        wireMock.stubFor(get(urlEqualTo(EXCHANGE_RATES_URL))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"rate\": 42.5}")));

        double rate = cosmicExchangeClient.getExchangeRate();

        assertThat(rate).isEqualTo(42.5);
        wireMock.verify(1, getRequestedFor(urlEqualTo(EXCHANGE_RATES_URL)));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenServiceReturnsEmptyBody() {
        wireMock.stubFor(get(urlEqualTo(EXCHANGE_RATES_URL))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")));

        assertThatThrownBy(() -> cosmicExchangeClient.getExchangeRate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Exchange service returned an empty response");
        wireMock.verify(1, getRequestedFor(urlEqualTo(EXCHANGE_RATES_URL)));
    }

    @Configuration
    @Import(CosmicExchangeClient.class)
    static class TestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}
