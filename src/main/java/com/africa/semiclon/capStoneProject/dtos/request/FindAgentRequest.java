package com.africa.semiclon.capStoneProject.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FindAgentRequest {
    private String email;
    private Long id;
}