package com.daelim.lotto.main.controller

import com.daelim.lotto.main.api.model.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.concurrent.thread
import kotlin.random.Random

@RestController
class Controller {
    private val users = mutableListOf<UserDto>()
    private val lottos = mutableListOf<LottoDto>()
    private val lottoAnswer = intArrayOf(15,16,17,25,30,31)
    private val lottoBonusAnswer = 32

    @PostMapping("/user/create")
    fun postUserDto(
        @RequestBody user: UserDto
    ): ResponseEntity<UserDto> {
        users.add(user)
        return ResponseEntity.ok().body(user)
    }
    @PostMapping("/user/login")
    fun login(
        @RequestBody user: UserRequestDto
    ): ResponseEntity<UserDto?> {
        val existUser = users.find { it.email == user.email && it.password == user.password }
        existUser?.let {
            return ResponseEntity.ok().body(existUser)
        }?: return ResponseEntity.notFound().build()
    }

    @GetMapping("lotto")
    fun getLotto(): ResponseEntity<LottoDto> {
        val numbers: MutableList<List<Int>> = mutableListOf()
        val lock = Any() // 동기화를 위한 락 객체

        val threads = List(5) { //쓰레드를 사용하여 5번 실행
            thread {
                val generatedNumbers = generateNumbers2()
                synchronized(lock) { //synchronized 를 사용해서 numbers 에 쓰기를 한 번에 하나의 스레드만 실행할 수 있게 변경
                    numbers.add(generatedNumbers)
                }
                println("Thread ${Thread.currentThread().name}: $generatedNumbers")
            }
        }

        threads.forEach { it.join() } // 모든 스레드가 완료될 때까지 기다림
        return ResponseEntity.ok(LottoDto(numbers))
    }

    @PostMapping("lotto")
    fun updateLotto(
        @RequestBody lotto: LottoDto
    ): ResponseEntity<LottoDto> {
        lottos.add(lotto)
        return ResponseEntity.ok(lotto)
    }

    @GetMapping("lotto/check")
    fun checkLotto(): ResponseEntity<List<LottoResultResponseDto>> {
        /*
        1등	6개 번호 일치
        2등	5개 번호 일치+ 보너스 번호일치	1
        3등	5개 번호 일치
        4등	4개 번호 일치
        5등	3개 번호 일치
        */
        val result =
        lottos.mapIndexed { index, lottoDto ->
            val lotto = lottoDto.numbers.map { numbers ->
                val correctNumbers = numbers.intersect(lottoAnswer.toList()).toList()
                val correctBonusNumber = numbers.firstOrNull { it == lottoBonusAnswer }
                val correctCount = correctNumbers.size
                val isBonusCorrect = correctBonusNumber != null
                val isCorrect = when (correctCount) {
                    6 -> "1등"
                    5 -> if (isBonusCorrect) "2등" else "3등"
                    4 -> "4등"
                    3 -> "5등"
                    else -> "낙첨"
                }
                LottoResult(
                    numbers,
                    LottoResultRequestDto(correctNumbers, correctBonusNumber),
                    isCorrect
                )
            }
            LottoResultResponseDto(
                index+1,
                LottoResultRequestDto(lottoAnswer.toList(),lottoBonusAnswer),
                lotto)
        }

        return ResponseEntity.ok(result)
    }

    fun generateNumbers(): List<Int> {
        return (1..45).shuffled().take(7).sorted()
    }

    fun generateNumbers2(): List<Int> {
        val numbers = mutableSetOf<Int>()
        while (numbers.size < 7) {
            val randomNumber = Random.nextInt(45) + 1
            numbers.add(randomNumber)
        }
        return numbers.sorted()
    }
}