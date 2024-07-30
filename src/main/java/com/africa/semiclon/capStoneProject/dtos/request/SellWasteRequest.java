package com.africa.semiclon.capStoneProject.dtos.request;

import com.africa.semiclon.capStoneProject.data.models.Category;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class SellWasteRequest {
    private Long userId;
    private Category type;
    private String quantity;
    private int wasteWeight;
}
