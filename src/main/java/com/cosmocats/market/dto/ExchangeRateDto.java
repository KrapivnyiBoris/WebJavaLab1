package com.cosmocats.market.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Response of the external cosmic exchange service. */
@Value
@Builder
@Jacksonized
public class ExchangeRateDto {
    double rate;
}
