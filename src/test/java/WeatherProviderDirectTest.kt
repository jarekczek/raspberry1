import org.junit.Test

class WeatherProviderDirectTest {
  @Test
  fun test() {
    val prov = WeatherProviderDirect()
    println(prov.getInfo())
    println(prov.getInfo())
    Thread.sleep(2000)
    println(prov.getInfo())
  }
}