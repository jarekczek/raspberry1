import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinEdge
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.wiringpi.Lcd
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class LcdNews() {
  private @Volatile var keepRunning = true
  val gpio = GpioFactory.getInstance()
  val redButtonPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_UP)
  val highlightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24)
  val motionPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23)
  val lcdHandle = Lcd.lcdInit(2, 8, 4,
    RaspiPin.GPIO_04.address, //rs (data/cmd)
    RaspiPin.GPIO_05.address, //enable
    RaspiPin.GPIO_26.address, //d4
    RaspiPin.GPIO_27.address, //d5
    RaspiPin.GPIO_28.address, //d6
    RaspiPin.GPIO_29.address, //d7
    0, 0, 0, 0
  )

  companion object {
    @JvmStatic fun main(args: Array<String>) {
      LcdNews().start()
    }
  }

  fun start() {
    highlightPin.low()
    gpio.addListener(RedButtonListener(lcdHandle), redButtonPin);
    gpio.addListener(MotionListener(), motionPin);

    val dateInfoProvider = object : InfoProvider {
      override fun getInfo(): String {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now())!!
      }
    }

    val infoProviders = listOf<InfoProvider>(
      WeatherProvider(),
      dateInfoProvider
      //NetworkInfoProvider
    )

    Lcd.lcdDisplay(lcdHandle, 1)
    while (keepRunning) {
      /*
      var i = 200
      while (i < 256) {
        Lcd.lcdPosition(lcdHandle, 0, 0)
        Lcd.lcdPuts(lcdHandle, "$i ")
        IntRange(0, 11).forEach {
          Lcd.lcdPutchar(lcdHandle, (i++).toByte())
        }
        Thread.sleep(3000)
      }
      */
      infoProviders.forEach {
        Lcd.lcdClear(lcdHandle)
        val info = try {
            it.getInfo()
          } catch(e: Exception) {
            e.printStackTrace()
            e.javaClass.simpleName
          }
        lcdPrint(lcdHandle, info)
        Thread.sleep(2000)
      }
    }
  }

  private fun lcdPrint(lcdHandle: Int, s: String) {
    Lcd.lcdPosition(lcdHandle, 0, 0)
    s.take(16).padEnd(16, ' ').forEach { ch ->
      val oneByte =
        if (ch.toString().equals("°"))
          223 /* 239 */.toByte() // znak stopien, ale to nieudane imitacje
        else
          (ch.toString() as java.lang.String).getBytes("iso-8859-1")[0].toByte()
      Lcd.lcdPutchar(lcdHandle, oneByte)
    }
  }

  private fun espeak(s: String) {
    Runtime.getRuntime().exec("espeak -s 300 " + s)
  }

  private fun shutdown() {
    Runtime.getRuntime().exec("shutdown -hP now")
  }

  inner class RedButtonListener(val lcdHandle: Int) : GpioPinListenerDigital {
    // Przycisk ma pull up, czyli nacisniecie powoduje falling.
    private var lastFalling : LocalDateTime = LocalDateTime.MAX
    override fun handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent?) {
      if (event?.edge == PinEdge.FALLING)
        lastFalling = LocalDateTime.now()
      else if (event?.edge == PinEdge.RISING) {
        if (lastFalling.plusSeconds(3).isBefore(LocalDateTime.now())) {
          lcdPrint(lcdHandle, "shutdown")
          keepRunning = false
          shutdown()
        } else {
          lcdPrint(lcdHandle, "quick press")
        }
      }
    }
  }

  inner class MotionListener() : GpioPinListenerDigital {
    var timer: Timer? = null
    override fun handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent?) {
      if (event?.edge == PinEdge.RISING) {
        highlightPin.high()
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
          override fun run() {
            highlightPin.low()
          }
        }, 30*1000)
      }
    }
  }
}

