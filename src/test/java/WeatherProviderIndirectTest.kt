import org.junit.Test

class WeatherProviderIndirectTest {
  @Test
  fun test() {
    val prov = WeatherProviderIndirect()
    println(prov.getInfo())
    println(prov.getInfo())
    Thread.sleep(2000)
    println(prov.getInfo())
  }
}