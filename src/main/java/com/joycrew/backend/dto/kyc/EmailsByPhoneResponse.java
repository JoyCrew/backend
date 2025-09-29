package com.joycrew.backend.dto.kyc;

import java.util.List;

public record EmailsByPhoneResponse(
        boolean success,
        int count,
        List<String> emails,
        String message
) {}