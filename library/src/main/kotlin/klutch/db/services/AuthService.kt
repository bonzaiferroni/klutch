package klutch.db.services

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger(AuthService::class.simpleName!!)

class AuthService(
    private val session: SessionService
) {

}