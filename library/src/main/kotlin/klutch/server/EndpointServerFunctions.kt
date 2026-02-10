package klutch.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.util.toMap
import kampfire.api.*
import kampfire.utils.ParameterMap

fun <Returned, E : GetEndpoint<Returned>> Route.getEndpoint(
    endpoint: E,
    block: suspend RoutingContext.(E) -> Returned?
) = get(endpoint.path) {
    standardResponse { block(endpoint) }
}

fun <Returned, E : GetByIdEndpoint<Returned>, IdType> Route.getEndpoint(
    endpoint: E,
    convertId: (String) -> IdType,
    block: suspend RoutingContext.(IdType, E) -> Returned?
) = get(endpoint.serverIdTemplate) {
    val id = call.getIdOrThrow(convertId)
    standardResponse { block(id, endpoint) }
}

fun <Returned, IdType: TableId<*>, E : GetByTableIdEndpoint<IdType, Returned>> Route.getEndpoint(
    endpoint: E,
    convertId: (String) -> IdType,
    block: suspend RoutingContext.(IdType, E) -> Returned?
) = get(endpoint.serverIdTemplate) {
    val id = call.getIdOrThrow(convertId)
    standardResponse { block(id, endpoint) }
}

fun <Sent, Returned, E : QueryEndpoint<Sent, Returned>> Route.queryEndpoint(
    endpoint: E,
    factory: (ParameterMap) -> Sent?,
    block: suspend RoutingContext.(Sent?, E) -> Returned?
) = get(endpoint.path) {
    standardResponse {
        val params = call.request.queryParameters.toMap()
        val sentValue = factory(params)
        block(sentValue, endpoint)
    }
}

inline fun <Returned, reified Sent : Any, E : PostEndpoint<Sent, Returned>> Route.postEndpoint(
    endpoint: E,
    noinline block: suspend RoutingContext.(Sent, E) -> Returned?
) = post(endpoint.path) {
    val sentValue = call.receive<Sent>()
    standardResponse { block(sentValue, endpoint) }
}

inline fun <reified Sent : Any, E : UpdateEndpoint<Sent>> Route.updateEndpoint(
    endpoint: E,
    noinline block: suspend RoutingContext.(Sent, E) -> Boolean?
) = put(endpoint.path) {
    val sentValue = call.receive<Sent>()
    standardResponse { block(sentValue, endpoint) }
}

inline fun <reified Sent: Any, E: DeleteEndpoint<Sent>> Route.deleteEndpoint(
    endpoint: E,
    noinline block: suspend RoutingContext.(Sent, E) -> Boolean?
) = delete(endpoint.path) {
    val sentValue = call.receive<Sent>()
    standardResponse { block(sentValue, endpoint) }
}

suspend fun <T> RoutingContext.standardResponse(block: suspend () -> T?) {
    if (call.response.isCommitted) return
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

fun <T> RoutingContext.readParamOrNull(param: EndpointParam<T>): T? {
    val str = call.request.queryParameters[param.key] ?: return null
    return param.read(str)
}

fun <T> RoutingContext.readParam(param: EndpointParam<T>) =
    readParamOrNull(param) ?: throw MissingParameterException(param.key)

class MissingParameterException(val param: String) : Exception("Missing required parameter: $param")

fun ApplicationCall.getIdOrThrow() = getIdOrThrow { it.toIntOrNull() }

fun <T: Any> ApplicationCall.getIdOrThrow(convertId: (String) -> T?): T {
    return this.parameters["id"]?.let { convertId(it) } ?: throw IllegalArgumentException("Id not found")
}

class UnauthorizedUserException(override val message: String? = null) : Exception()