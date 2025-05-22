package com.playus.userservice.domain.user.dto.partyuser;

import java.util.List;

public record PartyUserThumbnailUrlListResponse(
        List<String> thumbnailUrls
) { }
