import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.wiringpi.Gpio

object Blink {
  @JvmStatic fun main(args: Array<String>) {
    println("blink")
    val gpio = GpioFactory.getInstance()
    val outputPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08)
    outputPin.setShutdownOptions(false, PinState.LOW, null, PinMode.DIGITAL_OUTPUT)
    while (true) {
      outputPin.low()
      Thread.sleep(args[0].toLong())
      outputPin.high()
      Thread.sleep(args[1].toLong())
    }
  }
}