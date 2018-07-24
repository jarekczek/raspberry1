import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.util.*

/**
 * Gets weather info directly from internet server.
 */

class WeatherProviderDirect() : InternetInfoCachedProvider() {
  private val paramsBundle = ResourceBundle.getBundle("params")
  private val urlString: String = paramsBundle.getString("forecastUrlExternal")
  init {
    intervalSeconds = 15 * 60L
  }

  override fun getInfoFromInternet(): String {
    val url = URL(urlString)
    val doc = if (url.protocol.equals("file"))
      org.jsoup.Jsoup.parse(File(url.toURI()), "utf-8")
    else
      org.jsoup.Jsoup.parse(url, 5000)
    return doc.select(".czynnik").toList()
      .filter { arrayOf("Temperatura", "Wiatr").contains(it.text()) }
      .map { it.nextElementSibling().text() }
      .joinToString(", ")
      .replace(Regex("[NESW]+ przy "), "")
      .replace(" °C", "C")
  }

  companion object {
    @JvmStatic fun main(args: Array<String>) {
      println(WeatherProviderDirect().getInfo())
    }
  }
}
