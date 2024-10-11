package hellotest

import scala.collection.mutable

trait OutputHandler:
  // Function to print the word cloud
  def printWordCloud(
      wordFrequency: Map[String, Int],
      cloudSize: Int,
      minFrequency: Int,
      updateChart: Seq[(String, Int)] => Unit
  ): Unit = {
    // Sort words by frequency and apply filters (minFrequency and cloudSize)
    val sortedWords = wordFrequency.toSeq
      .filter(_._2 >= minFrequency) // Only include words with sufficient frequency
      .sortBy(-_._2)
      .take(cloudSize)

    val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
    println(wordCloud)

    // Update chart for dynamic visualization if not in test mode
    // updateChart(sortedWords)
  }