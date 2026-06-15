package klutch.server

import kampfire.api.toUsername

class UsernameGenerator {

    fun generate() = "${getAdjective()}${getNoun()}".toUsername()

    private fun getAdjective() = "TheWhole"
    private fun getNoun() = "Enchilada"
}