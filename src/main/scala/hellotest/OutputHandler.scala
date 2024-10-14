package hellotest

import scala.collection.mutable
import javax.swing.SwingUtilities


trait OutputHandler:
  // Function to print the word cloud
  def printWordCloud(
      wordFrequency: mutable.Map[String, Int],
      cloudSize: Int,
      minFrequency: Int,
      updateChart: Seq[(String, Int)] => Unit
  ): Unit = {
    // Sort words by frequency and apply filters (minFrequency and cloudSize)
    val sortedWords = wordFrequency.toSeq
      .filter(_._2 >= minFrequency) // Only include words with sufficient frequency
      .sortBy(-_._2) // Sort by frequency descending
      .take(cloudSize)

    // Format output as: word: frequency, word: frequency
    val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
    println(wordCloud)

    // Safely update the chart in the Swing thread
    SwingUtilities.invokeLater(() => {
      updateChart(sortedWords)
    })
  }