package com.mesalive.exception

import com.mesalive.dto.ErrorResponseDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MesaNaoEncontradaException::class, ReservaNaoEncontradaException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ErrorResponseDTO> {
        val status = HttpStatus.NOT_FOUND
        val body = ErrorResponseDTO(
            status = status.value(),
            error = "Recurso Não Encontrado",
            message = ex.message ?: "O recurso solicitado não foi encontrado."
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(ConflitoReservaException::class)
    fun handleConflict(ex: ConflitoReservaException): ResponseEntity<ErrorResponseDTO> {
        val status = HttpStatus.CONFLICT
        val body = ErrorResponseDTO(
            status = status.value(),
            error = "Conflito de Horário",
            message = ex.message ?: "Esta mesa já está reservada para este horário."
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(NegocioException::class)
    fun handleBusiness(ex: NegocioException): ResponseEntity<ErrorResponseDTO> {
        val status = HttpStatus.BAD_REQUEST
        val body = ErrorResponseDTO(
            status = status.value(),
            error = "Erro de Negócio",
            message = ex.message ?: "Requisição inválida devido a regras de negócio."
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDTO> {
        val status = HttpStatus.BAD_REQUEST
        val fieldErrors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Valor inválido"
            fieldErrors[fieldName] = errorMessage
        }
        val body = ErrorResponseDTO(
            status = status.value(),
            error = "Erro de Validação",
            message = "Um ou mais campos contêm erros de validação.",
            fieldErrors = fieldErrors
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponseDTO> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val body = ErrorResponseDTO(
            status = status.value(),
            error = "Erro Interno do Servidor",
            message = ex.message ?: "Ocorreu um erro inesperado no servidor."
        )
        return ResponseEntity.status(status).body(body)
    }
}
