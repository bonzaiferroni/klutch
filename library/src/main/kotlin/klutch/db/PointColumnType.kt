package klutch.db

import kampfire.model.Distance
import kampfire.model.GeoPoint
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.doubleParam
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.geometric.PGpoint
import kotlin.math.cos

object PointColumnType : ColumnType<PGpoint>() {
    override fun sqlType(): String = "POINT"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        stmt[index] = when (value) {
            is Pair<*, *> -> PGpoint((value.first as Number).toDouble(), (value.second as Number).toDouble())
            is PGpoint -> value
            else -> error("Unsupported value type for POINT: $value")
        }
    }

    override fun valueFromDB(value: Any): PGpoint = when (value) {
        is PGpoint -> value
        is String -> PGpoint(value)
        else -> error("Unexpected value from DB: $value")
    }
}

fun Column<PGpoint>.lng(): Expression<Double> =
    object : Expression<Double>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            queryBuilder.append("("); queryBuilder.append(this@lng); queryBuilder.append(")[1]::float8")
        }
    }

fun Column<PGpoint>.lat(): Expression<Double> =
    object : Expression<Double>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            queryBuilder.append("("); queryBuilder.append(this@lat); queryBuilder.append(")[2]::float8")
        }
    }

fun Column<PGpoint>.isNearEq(point: GeoPoint, errorMarginMeters: Double = 100.0): Op<Boolean> {
    val lat = point.lat

    val degLat = errorMarginMeters / METERS_PER_DEG_LAT
    val degLng = errorMarginMeters / (METERS_PER_DEG_LAT * cos(Math.toRadians(lat)).coerceAtLeast(1e-9))
    val radiusDegrees = minOf(degLat, degLng)

    val centerParam = QueryParameter(PGpoint(point.lng, point.lat), PointColumnType)
    val dist = InfixOpDouble(this, "<->", centerParam)

    return dist lessEq QueryParameter(radiusDegrees, DoubleColumnType())
}

private class InfixOpDouble(
    private val left: Expression<*>,
    private val op: String,
    private val right: Expression<*>,
) : Expression<Double>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("(")
        queryBuilder.append(left)
        queryBuilder.append(" ")
        queryBuilder.append(op)
        queryBuilder.append(" ")
        queryBuilder.append(right)
        queryBuilder.append(")")
    }
}


fun Table.point(name: String) = registerColumn(name, PointColumnType)

private const val METERS_PER_DEG_LAT = 111_320.0

private fun metersPerDegLngAtLat(lat: Double): Double =
    METERS_PER_DEG_LAT * cos(Math.toRadians(lat)).coerceAtLeast(1e-9)

fun Distance.toRadiusDegreesAt(lat: Double): Double {
    val rM = inMeters()
    val degLat = rM / METERS_PER_DEG_LAT
    val degLng = rM / (METERS_PER_DEG_LAT * cos(Math.toRadians(lat)).coerceAtLeast(1e-9))

    // Use the smaller degrees radius so circle in degrees fits inside both axes.
    return minOf(degLat, degLng)
}

private fun GeoPoint.boundingBox(radius: Distance): Pair<GeoPoint, GeoPoint> {
    val rM = radius.inMeters()
    val dLat = rM / METERS_PER_DEG_LAT
    val dLng = rM / metersPerDegLngAtLat(lat)

    val min = GeoPoint(lat - dLat, lng - dLng)
    val max = GeoPoint(lat + dLat, lng + dLng)
    return min to max
}

fun Query.withinBox(
    pointCol: Column<PGpoint>,
    center: GeoPoint,
    radius: Distance
): Query {
    val (min, max) = center.boundingBox(radius)

    val x = pointCol.lng()
    val y = pointCol.lat()

    return andWhere {
        (x greaterEq doubleParam(min.lng)) and (x lessEq doubleParam(max.lng)) and
                (y greaterEq doubleParam(min.lat)) and (y lessEq doubleParam(max.lat))
    }
}

fun Query.withinRadius(
    pointCol: Column<PGpoint>,
    center: GeoPoint,
    radius: Distance
): Query {
    val radiusDegrees = radius.toRadiusDegreesAt(center.lat)
    val centerParam = QueryParameter(PGpoint(center.lng, center.lat), PointColumnType)
    val dist = InfixOpDouble(pointCol, "<->", centerParam)

    return andWhere { dist lessEq QueryParameter(radiusDegrees, DoubleColumnType()) }
}