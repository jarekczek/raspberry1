import java.net.NetworkInterface

object NetworkInfoProvider : InfoProvider {
  override fun getInfo(): String {
        return NetworkInterface.getNetworkInterfaces().toList()
          .filter { it.name.startsWith("eth") }
          .flatMap { it.inetAddresses.toList() }
          .firstOrNull()
          ?.hostAddress ?: "no network"
  }

  @JvmStatic fun main(args: Array<String>) {
    println(NetworkInfoProvider.getInfo())
  }
}
