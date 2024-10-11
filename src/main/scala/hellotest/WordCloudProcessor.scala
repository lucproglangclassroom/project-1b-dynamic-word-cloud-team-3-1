package hellotest

import scala.collection.mutable

trait WordCloudProcessor:
  // Function to update the sliding window of words
  def updateWindow(
      word: String,
      window: List[String],
      wordFrequency: Map[String, Int],
      windowSize: Int
  ): (List[String], Map[String, Int]) = {
    val newWindow = word :: window
    val updatedFreq = wordFrequency.updated(word, wordFrequency.getOrElse(word, 0) + 1)

    // Remove the oldest word if the window exceeds size
    if (newWindow.size > windowSize) {
      val oldWord = newWindow.last
      val freq = updatedFreq(oldWord) - 1
      val newFreq = if (freq == 0) updatedFreq - oldWord else updatedFreq.updated(oldWord, freq)
      (newWindow.dropRight(1), newFreq)
    } else {
      (newWindow, updatedFreq)
    }
  }