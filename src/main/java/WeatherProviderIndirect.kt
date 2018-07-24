import org.jsoup.helper.HttpConnection
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.*

/**
 * Gets weather info from our server, which gets it from external server.
 * Thus we can limit network traffic to external server by routing it
 * through our server.
 */

class WeatherProviderIndirect() : InternetInfoCachedProvider(){
  private val paramsBundle = ResourceBundle.getBundle("params")
  private val urlString: String = paramsBundle.getString("forecastUrlOurLast")

  init {
    intervalSeconds = 30L
  }

  override fun getInfoFromInternet(): String {
    val url = URL(urlString)
    val istr = url.openStream()!!
    val eventString: String = istr.readBytes().toString(Charset.forName("UTF-8"))
      .replace("°", "")
      .replace("\n", "")
      .replace(Regex("[NESW]+ przy "), "")

    //println("event: _${eventString}_")
    return eventString
  }

  companion object {
    @JvmStatic fun main(args: Array<String>) {
      println(WeatherProviderIndirect().getInfo())
    }
  }
}
