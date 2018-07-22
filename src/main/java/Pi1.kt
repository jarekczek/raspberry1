import com.pi4j.io.gpio.*
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.io.gpio.impl.GpioPinImpl
import com.pi4j.io.gpio.impl.PinImpl
import com.pi4j.io.gpio.trigger.GpioTrigger
import com.pi4j.wiringpi.Gpio
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import javax.sound.sampled.AudioSystem

fun main(args: Array<String>) {
  microtiming()
}

fun testDelay(delay: Int) {
  Thread.sleep(100)
  val rep = if (delay < 100) 100 else 1
  val t0 = Gpio.micros()
  IntRange(0, rep).forEach { Gpio.delayMicroseconds(delay.toLong()) }
  val t1 = Gpio.micros()
  val elapsed = t1 - t0
  println("after delaying $delay micros elapsed time is " + (elapsed / rep) + ", lost " + ((t1 - t0)/rep - delay))
}

fun microtiming() {
  //println("setting priority returns " + Gpio.piHiPri(99))
  Gpio.wiringPiSetup()
  testDelay(1)
  testDelay(1)
  testDelay(5)
  testDelay(10)
  testDelay(30)
  testDelay(60)
  testDelay(99)
  testDelay(100)
  testDelay(200)
  testDelay(500)
  testDelay(1000)

  val t0 = Gpio.micros()
  IntRange(0, 1000).forEach { Gpio.micros() }
  println("one call to micros() took about " + ((Gpio.micros() - t0)/1000))

  var count = 0
  val finish = Semaphore(1)
  finish.drainPermits()
  val timer = Timer()
  var t1 = Gpio.micros()
  timer.scheduleAtFixedRate(object : TimerTask() {
    override fun run() {
      val t2 = Gpio.micros()
      println("time since last called: " + ((t2-t1)/1000.0))
      t1 = t2
      if (++count > 10) {
        finish.release()
        timer.cancel()
      }
    }
  }, 3, 3)
  finish.acquire()
  println("test end")
  System.exit(0)
}

fun music() {
  val clip = AudioSystem.getClip()
  clip.open(AudioSystem.getAudioInputStream(File("raspberry.wav")))
  clip.start()
  Thread.sleep(1000)
}

fun edgeListener() {
  val gpio = GpioFactory.getInstance()
  val outputPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29)
  outputPin.high()
  val inputPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, PinPullResistance.PULL_DOWN)
  inputPin.addListener(object : GpioPinListenerDigital {
    override fun handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent?) {
      println("event " + event?.edge + "/" + event?.state + "/" + event?.eventType)
    }
  })
  inputPin.setShutdownOptions(true)
  Thread.sleep(3000)
  inputPin.removeAllListeners()
  println("should finish now")
  gpio.shutdown()
}

fun maxFrequency() {
  println("kotlin ok 2")
  val gpio = GpioFactory.getInstance()



  /*
  val pinDef = PinImpl(RaspiGpioProvider.NAME, 21, "JC",
    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
    PinPullResistance.all())
  val newPin = GpioPinImpl(gpio, GpioFactory.getDefaultProvider(), pinDef)
  newPin.addListener(object : GpioPinListenerDigital {
    override fun handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent?) {
      println("listen")
    }
  })
  println("newPin: $newPin")
  */

  //Gpio.wiringPiISR(21, Gpio.INT_EDGE_RISING, { pin -> print("R") })

  IntRange(0, 1000).forEach {
    print(Gpio.digitalRead(21))
  }
  Gpio.wiringPiClearISR(21)
  System.exit(0)

  gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_05, PinMode.GPIO_CLOCK)
  val outputPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04)
  gpio.provisionedPins.forEach { pin ->
    println("$pin ${pin.mode}")
    pin.properties.forEach { println("property $it") }
  }
  var t0 = System.currentTimeMillis()
  gpio.setState(true, outputPin)
  println("elapsed 1: " + (System.currentTimeMillis() - t0))

  t0 = System.currentTimeMillis()
  gpio.setState(false, outputPin)
  println("elapsed 2: " + (System.currentTimeMillis() - t0))

  t0 = System.currentTimeMillis()
  val c = 100000
  IntRange(0, c).forEach {
    gpio.setState(true, outputPin)
    gpio.setState(false, outputPin)
  }
  println("elapsed $c: " + (System.currentTimeMillis() - t0))
}
