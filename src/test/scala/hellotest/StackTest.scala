package hellotest

// example straight from scalatest.org

import scala.collection.mutable.Stack

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Suite
import org.scalatest.matchers.must.Matchers.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import scala.collection.mutable

// class StackSpec extends AnyFlatSpec with Suite:

//   "A Stack" should "pop values in last-in-first-out order" in:
//     val stack = Stack.empty[Int]
//     stack.push(1)
//     stack.push(2)
//     stack.pop() must equal(2)
//     stack.pop() must equal(1)

//   it should "throw NoSuchElementException if an empty stack is popped" in:
//     val emptyStack = Stack.empty[Int]
//     an[NoSuchElementException] must be thrownBy:
//       emptyStack.pop()

// end StackSpec

class WordCloudSpec extends AnyFlatSpec:

  // Test for the updateWindow function
  "updateWindow" should "update the window and word frequency correctly" in {
    val window = mutable.Queue[String]()
    val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)
    val windowSize = 3

    // Add the word 'hello'
    hellotest.Main.updateWindow("hello", window, wordFrequency, windowSize)
    wordFrequency("hello") must equal(1)
    window.size must equal(1)

    // Add the word 'world'
    hellotest.Main.updateWindow("world", window, wordFrequency, windowSize)
    wordFrequency("world") must equal(1)
    wordFrequency("hello") must equal(1)
    window.size must equal(2)

    // Add the word 'scala'
    hellotest.Main.updateWindow("scala", window, wordFrequency, windowSize)
    wordFrequency("scala") must equal(1)
    window.size must equal(3)

    // Add a new word 'test' which should evict 'hello'
    hellotest.Main.updateWindow("test", window, wordFrequency, windowSize)
    wordFrequency("test") must equal(1)
    wordFrequency.getOrElse("hello", 0) must equal(0)  // 'hello' should be evicted
    window.size must equal(3)
  }

  // Test for the processInput function
  "processInput" should "correctly process input and update the word cloud" in {
    val window = mutable.Queue[String]()
    val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)
    val windowSize = 3
    val cloudSize = 3
    val minFrequency = 1
    val everyKSteps = 1
    val minLength = 3
    val ignoreList = Set("the", "and")
    val input = List("Hello world scala", "world scala test", "and the").iterator

    var capturedChartUpdates = List[Seq[(String, Int)]]()

    // Dummy chart update function for capturing updates
    def updateChartStub(sortedWords: Seq[(String, Int)]): Unit = {
      capturedChartUpdates :+= sortedWords
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
    wordFrequency("world") must equal(1)
    wordFrequency("scala") must equal(1)
    wordFrequency("test") must equal(1)
    wordFrequency.getOrElse("hello", 0) must equal(0)

    // Check if the chart was updated correctly
    capturedChartUpdates.nonEmpty must equal(true)
    capturedChartUpdates.last must contain allOf (("world", 1), ("scala", 1))
  }

end WordCloudSpec
