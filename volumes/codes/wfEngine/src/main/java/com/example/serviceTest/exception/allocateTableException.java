package com.example.serviceTest.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "静态分配错误")
public class allocateTableException extends RuntimeException {

}
