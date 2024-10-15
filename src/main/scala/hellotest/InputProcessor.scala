package hellotest
import scala.collection.mutable

trait InputProcessor { self: WordCloudProcessor with OutputHandler =>
  def processInput(
      lines: Iterator[String],
      minLength: Int,
      window: List[String], // Change to immutable List
      wordFrequency: Map[String, Int], // Change to immutable Map
      windowSize: Int,
      everyKSteps: Int,
      cloudSize: Int,
      minFrequency: Int,
      ignoreList: Set[String],
      updateChart: Seq[(String, Int)] => Unit
    ): Unit = {
      var steps = 0

      // Stream processing for input
      val words = 
        import scala.language.unsafeNulls
        lines
        .flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+"))
        .map(_.toLowerCase)
        .filter(word => word != null && word.length >= minLength && !ignoreList.contains(word)) // Filter words

      // Iterating over each word while immutably updating window and word frequency
      var currentWindow = window
      var currentWordFrequency = wordFrequency

      // Convert immutable Map to mutable Map before passing to printWordCloud
      words.foreach { word =>

        // Check for I/O errors like SIGPIPE
        import scala.language.unsafeNulls
        if (System.out.checkError()) {
          println("SIGPIPE detected, exiting.")
          System.exit(1)
        }

        val (newWindow, newWordFrequency) = updateWindowImmutable(word, currentWindow, currentWordFrequency, windowSize)
        currentWindow = newWindow
        currentWordFrequency = newWordFrequency
        steps += 1

        // Update and print word cloud every `everyKSteps`
        if (currentWindow.size >= windowSize && steps % everyKSteps == 0) {
          val mutableWordFrequency = mutable.Map[String, Int]() ++= currentWordFrequency
          printWordCloud(mutableWordFrequency, cloudSize, minFrequency, updateChart)
        }
      }
    }
}