import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.util.*

class WeatherProvider() : InfoProvider {
  private val paramsBundle = ResourceBundle.getBundle("params")
  private val urlString: String = paramsBundle.getString("forecastUrl")
  var lastInfo: String? = null
  var lastTime: LocalDateTime? = null

  override fun getInfo(): String {
    if (lastInfo == null || lastTime!!.plusMinutes(15).isBefore(LocalDateTime.now())) {
      lastInfo = getInfoFromInternet()
      lastTime = LocalDateTime.now()
      return try {
        getInfoFromInternet()
      } catch(e: Exception) {
        e.printStackTrace()
        e.javaClass.simpleName
      }
    } else {
      return lastInfo!!
    }
  }

  fun getInfoFromInternet(): String {
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
      println(WeatherProvider().getInfo())
    }
  }
}
