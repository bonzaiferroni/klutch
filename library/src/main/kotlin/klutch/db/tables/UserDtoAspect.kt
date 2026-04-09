package klutch.db.tables

//object UserDtoAspect : Aspect<UserDtoAspect, UserOld>(
//    BasicUserTable,
//    ResultRow::toUserDto
//) {
//    val id = add(BasicUserTable.id)
//    val username = add(BasicUserTable.username)
//    val roles = add(BasicUserTable.roles)
//    val avatarUrl = add(BasicUserTable.avatarUrl)
//    val createdAt = add(BasicUserTable.createdAt)
//    val updatedAt = add(BasicUserTable.updatedAt)
//}
//
//fun ResultRow.toUserDto() = UserOld(
//    userId = UserId(this[UserDtoAspect.id].value.toStringId()),
//    username = this[UserDtoAspect.username],
//    roles = this[UserDtoAspect.roles].map { UserRole.valueOf(it) }.toSet(),
//    avatarUrl = this[UserDtoAspect.avatarUrl],
//    createdAt = this[UserDtoAspect.createdAt],
//    updatedAt = this[UserDtoAspect.updatedAt],
//)

