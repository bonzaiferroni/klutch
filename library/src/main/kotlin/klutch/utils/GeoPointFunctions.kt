package klutch.utils

import kampfire.model.GeoPoint
import org.postgresql.geometric.PGpoint

fun GeoPoint.toPGpoint() = PGpoint(x, y)
fun PGpoint.toGeoPoint() = GeoPoint(x, y)