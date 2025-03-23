package com.Misbra.Component;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BilingualError {
    private String english;
    private String arabic;
}