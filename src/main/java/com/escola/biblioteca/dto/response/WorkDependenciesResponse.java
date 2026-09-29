package com.escola.biblioteca.dto.response;

import java.util.List;

public record WorkDependenciesResponse(
        List<ItemSummary> items,
        List<HoldSummary> holds) {

    public record ItemSummary(String id, String barcode, String callNumber, String status, String libraryName) {}

    public record HoldSummary(String id, String patronName, String libraryName, String status, int position) {}
}
