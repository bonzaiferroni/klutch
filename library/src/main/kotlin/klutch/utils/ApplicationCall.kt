package klutch.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.RoutingContext
import kabinet.model.UserId
import klutch.db.services.UserApiService
import klutch.server.CLAIM_ROLES
import klutch.server.CLAIM_USERNAME

