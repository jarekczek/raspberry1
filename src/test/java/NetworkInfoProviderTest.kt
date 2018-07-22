import org.junit.Test
import java.time.LocalDateTime

class NetworkInfoProviderTest {
  @Test
  fun test() {
    println(NetworkInfoProvider.getInfo())
  }
}