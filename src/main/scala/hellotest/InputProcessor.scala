package hellotest
import scala.collection.mutable
import scala.language.unsafeNulls

trait InputProcessor { self: WordCloudProcessor with OutputHandler =>
  // Recursive function to process the input and update the word cloud
  def processInput(
      lines: Iterator[String],
      minLength: Int,
      window: List[String],
      wordFrequency: Map[String, Int],
      windowSize: Int,
      everyKSteps: Int,
      cloudSize: Int,
      minFrequency: Int,
      ignoreList: Set[String],
      updateChart: Seq[(String, Int)] => Unit,
      steps: Int = 0
  ): Unit = {
    if (lines.hasNext) {
      val currentLine = lines.next()
      val words = Option(currentLine).getOrElse("").split("(?U)[^\\p{Alpha}0-9']+").nn.map(_.nn.toLowerCase.nn)
        .filter(word => word.length >= minLength && !ignoreList.contains(word))

      val (updatedWindow, updatedFrequency) = words.foldLeft((window, wordFrequency)) {
        case ((w, f), currentWord) => updateWindow(currentWord, w, f, windowSize)
      }

      if (updatedWindow.size >= windowSize && steps % everyKSteps == 0) {
        printWordCloud(updatedFrequency, cloudSize, minFrequency, updateChart)
      }

      processInput(lines, minLength, updatedWindow, updatedFrequency, windowSize, everyKSteps, cloudSize, minFrequency, ignoreList, updateChart, steps + 1)
    }
  }
}
