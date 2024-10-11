package hellotest

import org.knowm.xchart.{BitmapEncoder, PieChart, PieChartBuilder, SwingWrapper}
import org.knowm.xchart.style.PieStyler
import scala.io.Source

import mainargs.{main, arg, ParserForMethods, Flag}

object Main extends InputProcessor with WordCloudProcessor with OutputHandler:

  // Create the chart and GUI
  def createChart(): (PieChart, SwingWrapper[PieChart]) = {
    import scala.language.unsafeNulls
    val chart = new PieChartBuilder().width(800).height(600).title("Word Cloud").build().nn
    val sw = new SwingWrapper(chart)
    sw.displayChart()
    (chart, sw)
  }

  // Function to update the dynamic chart visualization
  def updateChart(chart: PieChart, sw: SwingWrapper[PieChart])(sortedWords: Seq[(String, Int)]): Unit = {
    chart.getSeriesMap.nn.clear() // Clear the previous chart
    sortedWords.foreach { case (word, freq) =>
      chart.addSeries(word, freq)
    }
    // Customize chart style
    chart.getStyler.nn.setLegendVisible(false)
    chart.getStyler.nn.setLabelType(PieStyler.LabelType.Name)
    chart.getStyler.nn.setLabelsDistance(0.75)
    chart.getStyler.nn.setPlotContentSize(0.7)
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
    val logger = Option(org.log4s.getLogger).getOrElse {
      throw new IllegalStateException("Logger could not be initialized")
    }
    logger.debug(f"howMany=$cloudSize minLength=$minLength lastNWords=$windowSize everyKSteps=$everyKSteps minFrequency=$minFrequency")

    // Initialize window and frequency map as immutable
    val window = List.empty[String]
    val wordFrequency = Map.empty[String, Int]

    // Load the ignore list from the file if provided, otherwise use an empty set
    val ignoreList: Set[String] = ignoreFilePath
      .flatMap { path => Option(Source.fromFile(path)).map(_.getLines().map(_.nn.trim.nn.toLowerCase.nn).toSet) }
      .getOrElse(Set.empty[String]) // Use empty set if ignoreFilePath is None

    val lines = Source.stdin.getLines()

    val updateChartFn: Seq[(String, Int)] => Unit =
      if (test.value) _ => () // No-op if test flag is set
      else {
        // Create the chart and GUI if not in test mode
        val (chart, sw) = createChart()
        updateChart(chart, sw) // Use the updateChart function
      }

    // Process input and update word cloud
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

  def main(args: Array[String]): Unit = {
    val _ = ParserForMethods(this).runOrExit(args.toIndexedSeq)
  }

end Main