package com.study.common.exception

open class BusinessException(
    val status: Int,
    override val message: String
) : RuntimeException(message)
