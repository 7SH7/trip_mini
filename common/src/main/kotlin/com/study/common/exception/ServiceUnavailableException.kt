package com.study.common.exception

class ServiceUnavailableException(service: String) :
    BusinessException(503, "$service is unavailable")
