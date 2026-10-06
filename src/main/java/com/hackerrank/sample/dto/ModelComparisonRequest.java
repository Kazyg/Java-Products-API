package com.hackerrank.sample.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ModelComparisonRequest {
    @NotEmpty(message = "ids must not be empty")
    private List<@NotNull(message = "ids must not contain null values") Long> ids;

    public ModelComparisonRequest() {
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
