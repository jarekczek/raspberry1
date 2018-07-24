import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.util.*

/**
 * Reads data from external server, caching it to make less internet requests.
 *
 */

abstract class InternetInfoCachedProvider() : InfoProvider {
  var lastInfo: String? = null
  var lastTime: LocalDateTime? = null
  protected var intervalSeconds = 60L

  override fun getInfo(): String {
    if (lastInfo == null || lastTime!!.plusSeconds(intervalSeconds).isBefore(LocalDateTime.now())) {
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

  abstract fun getInfoFromInternet(): String
}
