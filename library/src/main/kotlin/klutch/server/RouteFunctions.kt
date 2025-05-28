package klutch.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kabinet.api.*

fun <Returned, E : Endpoint<Returned>> Route.get(
    endpoint: E,
    block: suspend RoutingContext.(E) -> Returned?
) = get(endpoint.path) {
    standardResponse { block(endpoint) }
}

fun <Returned, E : GetByIdEndpoint<Returned>, IdType> Route.getById(
    endpoint: E,
    convertId: (String) -> IdType,
    block: suspend RoutingContext.(IdType, E) -> Returned?
) = get(endpoint.serverIdTemplate) {
    val id = call.getIdOrThrow(convertId)
    standardResponse { block(id, endpoint) }
}

inline fun <Returned, reified Sent : Any, E : PostEndpoint<Sent, Returned>> Route.post(
    endpoint: E,
    noinline block: suspend RoutingContext.(Sent, E) -> Returned?
) = post(endpoint.path) {
    val sentValue = call.receive<Sent>()
    standardResponse { block(sentValue, endpoint) }
}

inline fun <reified Sent : Any, E : UpdateEndpoint<Sent>> Route.update(
    endpoint: E,
    noinline block: suspend RoutingContext.(Sent, E) -> Boolean?
) = put(endpoint.path) {
    val sentValue = call.receive<Sent>()
    standardResponse { block(sentValue, endpoint) }
}

inline fun <reified Sent: Any, E: DeleteEndpoint<Sent>> Route.delete(
    endpoint: E,
    noinline block: suspend RoutingContext.(Sent, E) -> Boolean?
) = delete(endpoint.path) {
    val sentValue = call.receive<Sent>()
    standardResponse { block(sentValue, endpoint) }
}

suspend fun <T> RoutingContext.standardResponse(block: suspend () -> T?) {
    try {
        val value = block()
        if (value != null) {
            call.respond(HttpStatusCode.OK, value)
        } else if (!call.response.isCommitted) {
            call.respond(HttpStatusCode.NotFound)
        }
    } catch (e: MissingParameterException) {
        call.respond(HttpStatusCode.BadRequest, "Missing required parameter: ${e.param}")
    } catch (e: UnauthorizedUserException) {
        call.respond(HttpStatusCode.Forbidden, e.message ?: "Request unauthorized for this user")
    }
}

fun <T> EndpointParam<T>.readParamOrNull(call: RoutingCall): T? {
    val str = call.request.queryParameters[this.key] ?: return null
    return this.read(str)
}

fun <T> EndpointParam<T>.readParam(call: RoutingCall): T =
    this.readParamOrNull(call) ?: throw MissingParameterException(this.key)

class MissingParameterException(val param: String) : Exception("Missing required parameter: $param")

fun ApplicationCall.getIdOrThrow() = getIdOrThrow { it.toIntOrNull() }

fun <T: Any> ApplicationCall.getIdOrThrow(convertId: (String) -> T?): T {
    return this.parameters["id"]?.let { convertId(it) } ?: throw IllegalArgumentException("Id not found")
}

class UnauthorizedUserException(override val message: String? = null) : Exception()