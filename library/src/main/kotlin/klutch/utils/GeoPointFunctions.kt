package klutch.utils

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import org.postgresql.geometric.PGpoint

fun GeoPoint.toPGpoint() = PGpoint(x, y)
fun PGpoint.toGeoPoint() = GeoPoint(x, y)
fun List<Double>.toGeoBounds() = GeoBounds(GeoPoint(this[0], this[1]), GeoPoint(this[2], this[3]))
fun GeoBounds.toList() = listOf(sw.lng, sw.lat, ne.lng, ne.lat)