import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherProviderTest {
  @Test
  fun test() {
    val prov = WeatherProvider("file:///c:/temp/1/weather.html")
    println(prov.getInfo())
    println(prov.getInfo())
    Thread.sleep(2000)
    println(prov.getInfo())
  }
}