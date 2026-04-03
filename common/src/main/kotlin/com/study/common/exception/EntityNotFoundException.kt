package com.study.common.exception

class EntityNotFoundException(entity: String, id: Any) :
    BusinessException(404, "$entity not found: $id")
