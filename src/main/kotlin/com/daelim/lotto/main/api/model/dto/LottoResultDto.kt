package com.daelim.lotto.main.api.model.dto

data class LottoResultResponseDto (
    val index: Int,
    val winningNumbers: LottoResultRequestDto,
    val results: List<LottoResult>,
)

data class LottoResultRequestDto (
    val numbers: List<Int>,
    val bonusNumber: Int?,
)

data class LottoResult (
    val numbers: List<Int>,
    val correctNumbers: LottoResultRequestDto,
    val result: String,
)

