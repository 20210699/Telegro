package com.telegro.telegro.global.apiPayLoad.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Error {
    /* 400 */
    BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, 40000, "적절하지 않은 요청입니다."),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, 40001, "포인트가 부족합니다."),

    /* 401 */
    INVALID_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, 40100, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, 40101, "만료된 토큰입니다."),
    INVALID_ID_PASSWORD(HttpStatus.UNAUTHORIZED, 40102, "아이디/비밀번호가 적절하지 않습니다."),

    /* 403 */
    NOT_AGREED_TERM(HttpStatus.FORBIDDEN, 40300, "이용약관 동의가 필요합니다"),
    WIRHDRAWN_USER_ERROR(HttpStatus.FORBIDDEN, 40301, "탈퇴 후 7일이 지나지 않은 재가입 사용자입니다."),
    FORBIDDEN_ACTION_ERROR(HttpStatus.FORBIDDEN, 40302, "관리자 권한이 필요합니다."),

    /* 404 */
    NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, 40400, "리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "사용자를 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "장바구니를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "주문을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "결제를 찾을 수 없습니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "공급업체를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "상품을 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "공지사항을 찾을 수 없습니다."),

    /* 405 */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "존재하지 않는 요청입니다."),

    /* 409 */
    NICKNAME_ALREADY_USED_ERROR(HttpStatus.CONFLICT, 40900, "이미 사용 중인 닉네임입니다."),
    COMPANY_NAME_ALREADY_USED_ERROR(HttpStatus.CONFLICT, 40901, "이미 사용 중인 상호입니다."),

    /* 500 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "서버 에러가 발생했습니다."),
    EXTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50001, "외부 API와 통신에 실패했습니다."),
    PAYMENT_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 50002, "결제 정보 조회 중 오류 발생했습니다.");


    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}