package klutch.server

import kampfire.api.toUsername

@Deprecated("use SessionService function")
class UsernameGenerator {

    fun generate() = "${getAdjective()}${getNoun()}".toUsername()

    private fun getAdjective() = "TheWhole"
    private fun getNoun() = "Enchilada"
}