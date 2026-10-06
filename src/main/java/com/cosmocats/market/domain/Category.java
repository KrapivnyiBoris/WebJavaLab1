package com.cosmocats.market.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Product category of the intergalactic marketplace. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private UUID id;
    private String name;
    private String description;
}
