package hellotest

// example straight from scalatest.org

import scala.collection.mutable.Stack

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Suite
import org.scalatest.matchers.must.Matchers.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class WordCloudSpec extends AnyFlatSpec with Matchers {

  // Test for the updateWindowImmutable function
  "updateWindowImmutable" should "update the window and word frequency correctly" in {
    val window = List[String]()
    val wordFrequency = Map[String, Int]().withDefaultValue(0)
    val windowSize = 3

    // Add the word 'hello'
    val (newWindow1, newWordFreq1) = hellotest.Main.updateWindowImmutable("hello", window, wordFrequency, windowSize)
    newWordFreq1("hello") must equal(1)
    newWindow1.size must equal(1)

    // Add the word 'world'
    val (newWindow2, newWordFreq2) = hellotest.Main.updateWindowImmutable("world", newWindow1, newWordFreq1, windowSize)
    newWordFreq2("world") must equal(1)
    newWordFreq2("hello") must equal(1)
    newWindow2.size must equal(2)

    // Add the word 'scala'
    val (newWindow3, newWordFreq3) = hellotest.Main.updateWindowImmutable("scala", newWindow2, newWordFreq2, windowSize)
    newWordFreq3("scala") must equal(1)
    newWindow3.size must equal(3)

    // Add a new word 'test' which should evict 'hello'
    val (newWindow4, newWordFreq4) = hellotest.Main.updateWindowImmutable("test", newWindow3, newWordFreq3, windowSize)
    newWordFreq4("test") must equal(1)
    newWordFreq4.getOrElse("hello", 0) must equal(0) // 'hello' should be evicted
    newWindow4.size must equal(3)
  }

  // Test for the processInput function
  "processInput" should "correctly process input and update the word cloud" in {
    val window = List[String]()
    val wordFrequency = Map[String, Int]().withDefaultValue(0)
    val windowSize = 3
    val cloudSize = 3
    val minFrequency = 1
    val everyKSteps = 1
    val minLength = 3
    val ignoreList = Set("the", "and")
    val input = List("hello world scala", "world scala test", "and the").iterator

    var capturedChartUpdates = List[Seq[(String, Int)]]()

    // Dummy chart update function for capturing updates
    def updateChartStub(sortedWords: Seq[(String, Int)]): Unit = {
      capturedChartUpdates :+= sortedWords
      // wordFrequency = sortedWords.toMap.withDefaultValue(0)
    }

    hellotest.Main.processInput(
      input,
      minLength,
      window,
      wordFrequency,
      windowSize,
      everyKSteps,
      cloudSize,
      minFrequency,
      ignoreList,
      updateChartStub
    )

    // Check the word frequencies after processing
    wordFrequency("world") must equal(0)
    wordFrequency("scala") must equal(0)
    wordFrequency("test") must equal(0)
    wordFrequency.getOrElse("hello", 0) must equal(0)

    // Check if the chart was updated correctly
    capturedChartUpdates.nonEmpty must equal(true)
    capturedChartUpdates.last must contain allOf (("world", 1), ("scala", 1), ("test", 1))
  }

}
