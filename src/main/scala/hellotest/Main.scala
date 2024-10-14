package hellotest

import scala.collection.mutable
import scala.io.StdIn
import scala.sys.process._
import mainargs.{main, arg, ParserForMethods, Flag}
import org.knowm.xchart.{PieChartBuilder, SwingWrapper}
import java.io.IOException

object Main extends InputProcessor with WordCloudProcessor with OutputHandler:

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
  // SIGPIPE implementation to exit gracefully.
  def main(args: Array[String]): Unit = {
    try {
      ParserForMethods(this).runOrExit(args.toIndexedSeq)
    } catch {
      case e: IOException =>
        System.err.nn.println(s"SIGPIPE / IOException occurred in main method: ${e.getMessage}")
        System.exit(1)
    }
  }

end Main