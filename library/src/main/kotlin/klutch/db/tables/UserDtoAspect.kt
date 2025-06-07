package klutch.db.tables

import klutch.db.Aspect
import kabinet.model.UserRole
import kabinet.model.User
import kabinet.utils.toInstantUtc
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.ResultRow

object UserDtoAspect : Aspect<UserDtoAspect, User>(
    UserTable,
    ResultRow::toUserDto
) {
    val id = add(UserTable.id)
    val username = add(UserTable.username)
    val roles = add(UserTable.roles)
    val avatarUrl = add(UserTable.avatarUrl)
    val createdAt = add(UserTable.createdAt)
    val updatedAt = add(UserTable.updatedAt)
}

fun ResultRow.toUserDto() = User(
    id = this[UserDtoAspect.id].value.toStringId(),
    username = this[UserDtoAspect.username],
    roles = this[UserDtoAspect.roles].map { UserRole.valueOf(it) }.toSet(),
    avatarUrl = this[UserDtoAspect.avatarUrl],
    createdAt = this[UserDtoAspect.createdAt].toInstantUtc(),
    updatedAt = this[UserDtoAspect.updatedAt].toInstantUtc(),
)

