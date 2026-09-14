package com.starter.app.core.common.exceptions

import com.starter.app.core.common.exceptions.AppCodes.UNDEFINED

class AppException(errorMsg: String? = null, val errorCode: Int = UNDEFINED) : Exception(errorMsg)