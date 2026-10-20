package com.cosmocats.market.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CosmicWordCheckValidatorTest {

    private final CosmicWordCheckValidator validator = new CosmicWordCheckValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "Anti-gravity Star Yarn",
            "COSMIC Galaxy Milk",
            "comet tail wand",
            "Deep SpAcE helmet",
            "Nebula Catnip",
            "Meteor Treats",
            "Scratching Planet",
            "Orbit Feeder",
            "Universe Collar",
            "Starlight"
    })
    void shouldReturnTrueWhenNameContainsCosmicTermIgnoringCase(String name) {
        assertThat(validator.isValid(name, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Boring mouse", "Plain bowl", "Cat collar", "123"})
    void shouldReturnFalseWhenNameContainsNoCosmicTerm(String name) {
        assertThat(validator.isValid(name, null)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldReturnTrueWhenNameIsNullOrBlankBecauseNotBlankChecksPresence(String name) {
        assertThat(validator.isValid(name, null)).isTrue();
    }
}
