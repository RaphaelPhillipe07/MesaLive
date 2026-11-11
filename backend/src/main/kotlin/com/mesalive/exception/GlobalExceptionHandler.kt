package com.mesalive.exception

import com.mesalive.dto.ErrorResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MesaNaoEncontradaException::class, ReservaNaoEncontradaException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.NOT_FOUND, "Recurso Não Encontrado", ex.message ?: "O recurso solicitado não foi encontrado.")
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.NOT_FOUND, "Recurso Não Encontrado", "O endpoint solicitado não existe.")
    }

    @ExceptionHandler(ConflitoReservaException::class)
    fun handleConflict(ex: ConflitoReservaException): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.CONFLICT, "Conflito de Horário", ex.message ?: "Esta mesa já está reservada para este horário.")
    }

    @ExceptionHandler(CredenciaisInvalidasException::class)
    fun handleInvalidCredentials(ex: CredenciaisInvalidasException): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.UNAUTHORIZED, "Não Autenticado", ex.message ?: "Usuário ou senha incorretos.")
    }

    @ExceptionHandler(NegocioException::class)
    fun handleBusiness(ex: NegocioException): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.BAD_REQUEST, "Erro de Negócio", ex.message ?: "Requisição inválida devido a regras de negócio.")
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.FORBIDDEN, "Acesso Negado", "Você não tem permissão para executar esta ação.")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDTO> {
        val fieldErrors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            fieldErrors[fieldName] = error.defaultMessage ?: "Valor inválido"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponseDTO(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Erro de Validação",
                message = "Um ou mais campos contêm erros de validação.",
                fieldErrors = fieldErrors
            )
        )
    }

    /**
     * JSON malformado, enum inválido ou campo obrigatório ausente no corpo chegam aqui.
     * Sem este handler o genérico abaixo os transformaria em 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.BAD_REQUEST, "Requisição Inválida", "O corpo da requisição está ausente ou malformado.")
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.BAD_REQUEST, "Requisição Inválida", "O parâmetro '${ex.name}' possui um valor inválido.")
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(ex: MissingServletRequestParameterException): ResponseEntity<ErrorResponseDTO> {
        return build(HttpStatus.BAD_REQUEST, "Requisição Inválida", "O parâmetro obrigatório '${ex.parameterName}' não foi informado.")
    }

    /**
     * Fallback: nunca devolve a mensagem interna ao cliente (evita vazar SQL, stack e nomes de classes).
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponseDTO> {
        log.error("Erro interno não tratado", ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno do Servidor", "Ocorreu um erro inesperado no servidor.")
    }

    private fun build(status: HttpStatus, error: String, message: String): ResponseEntity<ErrorResponseDTO> {
        return ResponseEntity.status(status).body(
            ErrorResponseDTO(status = status.value(), error = error, message = message)
        )
    }
}
