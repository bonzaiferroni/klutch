package klutch.utils

import kabinet.model.GeoPoint
import org.postgresql.geometric.PGpoint

fun GeoPoint.toPGpoint() = PGpoint(longitude, latitude)
fun PGpoint.toGeoPoint() = GeoPoint(x, y)