package com.cosmocats.market.service;

import java.util.List;

/** Framework-independent page of domain objects. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
