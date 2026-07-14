package klutch.db.services

import kampfire.model.UserRole
import kampfire.model.SignUpRequest
import kampfire.model.AuthUser
import kampfire.model.UserSeed
import klutch.server.generateSalt
import klutch.server.hashPassword
import klutch.server.toBase64
import klutch.utils.serverLog

// class AuthServiceLegacy<User: AuthUser, Id: AuthId>(
//     private val dao: AuthDao<User, Id>,
// ) {
//     suspend fun createUser(
//         request: SignUpRequest,
//         roles: Set<UserRole> = setOf(UserRole.User),
//     ): AuthId {
//         serverLog.logInfo("Creating user: ${request.username}")
//         validateUsername(request)
//         validateEmail(request)
//         validatePassword(request)
//
//         val salt = generateUniqueSalt()
//         val hashedPassword = hashPassword(request.password, salt)
//         val seed = UserSeed(
//             request = request,
//             salt = salt.toBase64(),
//             hashedPassword = hashedPassword,
//             roles = roles,
//             accountType = request.accountType
//         )
//
//         return dao.createUser(seed)
//     }
//
//     private suspend fun validateUsername(info: SignUpRequest) {
//         if (!info.username.validUsernameLength) throw IllegalArgumentException("Username should be least 3 characters.")
//         if (!info.username.validUsernameChars) throw IllegalArgumentException("Username has invalid characters.")
//         val id = dao.readIdByUsername(info.username)
//         if (id != null) throw IllegalArgumentException("Username already exists.")
//     }
//
//     private suspend fun validateEmail(info: SignUpRequest) {
//         val email = info.email ?: return // email is optional
//         if (!info.email.validEmail) throw IllegalArgumentException("Invalid email.")
//         val user = dao.readByUsernameOrEmail(email)
//         if (user != null) throw IllegalArgumentException("Email already exists.")
//     }
//
//     private fun validatePassword(info: SignUpRequest) {
//         if (!info.password.validPassword) throw IllegalArgumentException("Password is too weak.")
//     }
//
//     private suspend fun generateUniqueSalt(): ByteArray {
//         while (true) {
//             val salt = generateSalt()
//             val saltExists = dao.readSaltExists(salt.toBase64())
//             if (!saltExists) return salt
//         }
//     }
// }