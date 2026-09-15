package klutch.utils

import kampfire.model.GeoRect
import kampfire.model.GeoPoint
import org.postgresql.geometric.PGpoint

fun GeoPoint.toPGpoint() = PGpoint(x, y)
fun PGpoint.toGeoPoint() = GeoPoint(x, y)
fun List<Double>.toGeoBounds() = GeoRect(GeoPoint(this[0], this[1]), GeoPoint(this[2], this[3]))
fun GeoRect.toList() = listOf(sw.lng, sw.lat, ne.lng, ne.lat)