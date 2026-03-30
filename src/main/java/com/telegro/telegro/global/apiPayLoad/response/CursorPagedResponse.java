package com.telegro.telegro.global.apiPayLoad.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPagedResponse<T>(
        boolean hasNext,
        Cursor nextCursor,
        Long totalElements,
        List<T> content
) {
    public static <T> CursorPagedResponse<T> of(
            boolean hasNext,
            Cursor nextCursor,
            Long totalElements,
            List<T> content

    ) {
        return new CursorPagedResponse<>(hasNext, nextCursor, totalElements, content);
    }

    public static Cursor cursorOf(Long lastId, LocalDateTime lastCreatedAt) {
        if (lastId == null || lastCreatedAt == null) {
            return null;
        }
        return new Cursor(lastId, lastCreatedAt);
    }

    public record Cursor(
            Long lastId,
            LocalDateTime lastCreatedAt
    ) {
    }
}
