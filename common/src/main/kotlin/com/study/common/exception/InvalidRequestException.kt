package com.study.common.exception

class InvalidRequestException(message: String) :
    BusinessException(400, message)
