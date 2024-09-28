package hellotest
import scala.collection.mutable
import scala.io.StdIn
import scala.sys.process._
import mainargs.{main, arg, ParserForMethods, Flag}
import org.knowm.xchart.{PieChartBuilder, SwingWrapper}


object Main:

  // Function to update the sliding window of words
  def updateWindow(
      word: String,
      window: mutable.Queue[String],
      wordFrequency: mutable.Map[String, Int],
      windowSize: Int
  ): Unit = {
    window.enqueue(word)
    wordFrequency(word) += 1

    // Remove the oldest word if the window exceeds size
    if (window.size > windowSize) {
      val oldWord = window.dequeue()
      wordFrequency(oldWord) -= 1
      if (wordFrequency(oldWord) == 0) {
        wordFrequency.remove(oldWord)
      }
    }
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
      .sortBy(-_._2)
      .take(cloudSize)

    val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
    println(wordCloud)

    // Update chart for dynamic visualization if not in test mode
    updateChart(sortedWords)
  }

  // Function to process the input and update the word cloud
  def processInput(
      lines: Iterator[String],
      minLength: Int,
      window: mutable.Queue[String],
      wordFrequency: mutable.Map[String, Int],
      windowSize: Int,
      everyKSteps: Int,
      cloudSize: Int,
      minFrequency: Int,
      ignoreList: Set[String],
      updateChart: Seq[(String, Int)] => Unit
  ): Unit = {
    var steps = 0
    val words = 
      import scala.language.unsafeNulls
      lines
      .flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+"))
      .map(_.toLowerCase)
      // .filter(word => word != null && word.length >= minLength)
      .filter(word => word != null && word.length >= minLength && !ignoreList.contains(word)) // Ignore words from ignore list


    words.foreach { word =>
      updateWindow(word, window, wordFrequency, windowSize)
      steps += 1

      // Update and print word cloud every `everyKSteps`
      if (window.size >= windowSize && steps % everyKSteps == 0) {
        printWordCloud(wordFrequency, cloudSize, minFrequency, updateChart)
      }
    }
  }

  // Create the chart and GUI
  def createChart(): (org.knowm.xchart.PieChart, SwingWrapper[org.knowm.xchart.PieChart]) = {
    val chart = 
      import scala.language.unsafeNulls
      new PieChartBuilder().width(800).height(600).title("Word Cloud").build()

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

    // Initialize window and frequency map
    val window = mutable.Queue[String]()
    val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)

    // Load the ignore list from the file
    // val ignoreList = scala.io.Source.fromFile(ignoreFilePath).getLines().map(_.trim.toLowerCase).toSet
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
/*
object Main:

  var cloudSize = 10
  var minLength = 6
  var windowSize = 1000
  var everyKSteps = 10
  var minFrequency = 3

  @main
  def run(
    @arg(short = 'c', doc = "size of the sliding word cloud") cloudSize: Int = 10,
    @arg(short = 'l', doc = "minimum word length to be considere") minLength: Int = 6,
    @arg(short = 'w', doc = "size of the sliding FIFO queue") windowSize: Int = 1000,
    @arg(short = 's', doc = "number of steps between word cloud updates") everyKSteps: Int = 10,
    @arg(short = 'f', doc = "minimum frequency for a word to be included in the cloud") minFrequency: Int = 3): Unit = {
    // val logger = org.log4s.getLogger
    // logger.debug(f"howMany=$cloudSize minLength=$minLength lastNWords=$windowSize everyKSteps=$everyKSteps minFrequency=$minFrequency")  
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args) 

    println(s"Cloud size: $cloudSize, Min length: $minLength, Window size: $windowSize")


    // Sliding window to track the last N words
    val window = mutable.Queue[String]()

    // Map to track word frequencies within the window
    val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)

    def updateWindow(word: String): Unit = {
      window.enqueue(word)
      wordFrequency(word) += 1

      // Remove the oldest word if the window exceeds size
      if (window.size > windowSize) {
        val oldWord = window.dequeue()
        wordFrequency(oldWord) -= 1
        if (wordFrequency(oldWord) == 0) {
          wordFrequency.remove(oldWord)
        }
      }
    }

    // Function to print the word cloud
    def printWordCloud(): Unit = {
      // val sortedWords = wordFrequency.toSeq.sortBy(-_._2).take(cloudSize)

      // Sort words by frequency and apply filters (minFrequency and cloudSize)
      val sortedWords = wordFrequency.toSeq
        .filter(_._2 >= minFrequency) // Only include words with sufficient frequency
        .sortBy(-_._2)
        .take(cloudSize)

      val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
      println(wordCloud)

      // Update chart for dynamic visualization
      updateChart(sortedWords)
    }

    // Create the chart and GUI
    val chart = 
      import scala.language.unsafeNulls
      new PieChartBuilder().width(800).height(600).title("Word Cloud").build()
    val sw = new SwingWrapper(chart)
    sw.displayChart()
    // Function to update the dynamic chart visualization
    def updateChart(sortedWords: Seq[(String, Int)]): Unit = {
      import scala.language.unsafeNulls
      chart.getSeriesMap.clear() // Clear the previous chart
      sortedWords.foreach { case (word, freq) =>
        chart.addSeries(word, freq)
      }
      sw.repaintChart() // Refresh the chart window
    }

    var steps = 0
    
    val lines = scala.io.Source.stdin.getLines()
    val words = 
      import scala.language.unsafeNulls
      lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+"))
      .map(_.toLowerCase)
      .filter(_.length >= minLength)
    words.foreach { word =>
      updateWindow(word)
      steps += 1
      // if (window.size >= windowSize) {
      //   printWordCloud()
      // }
      // Update and print word cloud every `everyKSteps`
      if (window.size >= windowSize && steps % everyKSteps == 0) {
        printWordCloud()
      }
    }
*/
end Main



