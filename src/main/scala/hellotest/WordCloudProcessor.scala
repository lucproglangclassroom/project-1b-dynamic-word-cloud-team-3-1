package hellotest

import scala.collection.mutable

trait WordCloudProcessor:
  // Function to update the sliding window and word frequency map using immutable structures
  def updateWindowImmutable(
    word: String,
    window: List[String],
    wordFrequency: Map[String, Int],
    windowSize: Int
  ): (List[String], Map[String, Int]) = {
  
    // If window is full, remove the oldest word before adding the new one
    val (newWindow, updatedFreq) = if (window.size >= windowSize) {
      val oldestWord = window.last // Get the oldest word in the window (last element)
      val updatedFreq = wordFrequency.updatedWith(oldestWord) {
        case Some(1) => None // Remove word if frequency is 1
        case Some(freq) => Some(freq - 1)
        case None => None
      }
      (word :: window.dropRight(1), updatedFreq) // Drop the last word from the window
    } else {
      (word :: window, wordFrequency)
    }

    // Update frequency map immutably for the new word
    val finalFreq = updatedFreq.updatedWith(word) {
      case Some(freq) => Some(freq + 1)
      case None => Some(1)
    }

    (newWindow, finalFreq)
  }
