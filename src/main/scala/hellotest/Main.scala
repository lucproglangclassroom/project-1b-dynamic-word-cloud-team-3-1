package hellotest

package hellotest
import scala.collection.mutable
import scala.io.StdIn
import scala.sys.process._
import mainargs.{main, arg, ParserForMethods, Flag}
import org.knowm.xchart.{PieChartBuilder, SwingWrapper}
import javax.swing.SwingUtilities

object Main:

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

  // Create the chart and GUI
  def createChart(): (org.knowm.xchart.PieChart, SwingWrapper[org.knowm.xchart.PieChart]) = {
    val chart = 
      import scala.language.unsafeNulls
      new PieChartBuilder().nn.width(800).height(600).title("Word Cloud").build()

    val sw = new SwingWrapper(chart)
    sw.displayChart()
    (chart, sw)
  }

  // Function to update the dynamic chart visualization
  def updateChart(chart: org.knowm.xchart.PieChart, sw: SwingWrapper[org.knowm.xchart.PieChart])(sortedWords: Seq[(String, Int)]): Unit = {
    import scala.language.unsafeNulls
    chart.getSeriesMap.clear() // Clear the previous chart
    sortedWords.foreach { case (word, freq) =>
      chart.addSeries(word, freq)
    }
    sw.repaintChart() // Refresh the chart window
  }    


  @main
  def run(
      @arg(short = 'c', doc = "size of the sliding word cloud") cloudSize: Int = 10,
      @arg(short = 'l', doc = "minimum word length to be considered") minLength: Int = 6,
      @arg(short = 'w', doc = "size of the sliding FIFO queue") windowSize: Int = 1000,
      @arg(short = 's', doc = "number of steps between word cloud updates") everyKSteps: Int = 10,
      @arg(short = 'f', doc = "minimum frequency for a word to be included in the cloud") minFrequency: Int = 3,
      @arg(short = 'i', doc = "file path for words to ignore") ignoreFilePath: Option[String] = None,
      @arg(short = 't', doc = "test flag to run without visualization") test: Flag = Flag()
  ): Unit = {
    val logger = 
      import scala.language.unsafeNulls
      org.log4s.getLogger
    logger.debug(f"howMany=$cloudSize minLength=$minLength lastNWords=$windowSize everyKSteps=$everyKSteps minFrequency=$minFrequency")  

    val window = List[String]() // Using immutable List instead of mutable Queue
    val wordFrequency = Map[String, Int]().withDefaultValue(0) // Using immutable Map

    // Load the ignore list from the file if provided, otherwise use an empty set
    val ignoreList: Set[String] = ignoreFilePath.map { path =>
      import scala.language.unsafeNulls
      scala.io.Source.fromFile(path).getLines().map(_.trim.toLowerCase).toSet
    }.getOrElse(Set.empty[String]) // Use empty set if ignoreFilePath is None

    val updateChartFn: Seq[(String, Int)] => Unit =
      if (test.value) _ => () // No-op if test flag is set
      else {
        // Create the chart and GUI if not in test mode
        val (chart, sw) = createChart()
        updateChart(chart, sw)
      }
    
    // Process input and update word cloud
    val lines = scala.io.Source.stdin.getLines()
    processInput(
      lines,
      minLength,
      window,
      wordFrequency,
      windowSize,
      everyKSteps,
      cloudSize,
      minFrequency,
      ignoreList,
      updateChartFn
    )
  }
  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args.toIndexedSeq)

end Main
