package hellotest
import scala.collection.mutable
import scala.io.StdIn
import scala.sys.process._
import mainargs.{main, arg, ParserForMethods}
import org.log4s._
import org.slf4j.LoggerFactory


object Main:
  
  def main(args: Array[String]) = 
  // def main(args: Array[String]) = ParserForMethods(this).runOrExit(args.toIndexedSeq)
  //   // private val logger: org.log4s.Logger = Option(org.log4s.getLogger).getOrElse(LoggerFactory.getLogger("Main"))
  //   private val logger: org.slf4j.Logger = LoggerFactory.getLogger("Main")
  //   // internal main method with arguments annotated for parsing
  //   @main
  //   def run(
  //       @arg(short = 'c', doc = "size of the sliding word cloud") cloudSize: Int = 10,
  //       @arg(short = 'l', doc = "minimum word length to be considere") minLength: Int = 6,
  //       @arg(short = 'w', doc = "size of the sliding FIFO queue") windowSize: Int = 1000,
  //       @arg(short = 's', doc = "number of steps between word cloud updates") everyKSteps: Int = 10,
  //       @arg(short = 'f', doc = "minimum frequency for a word to be included in the cloud") minFrequency: Int = 3) =

  //       logger.debug(f"howMany=$cloudSize minLength=$minLength lastNWords=$windowSize everyKSteps=$everyKSteps minFrequency=$minFrequency")
      
    // Default values
    var cloudSize = 10
    var minLength = 6
    var windowSize = 1000

    // Parse command-line arguments
    // args.sliding(2, 2).foreach {
    //   case Array("--cloud-size", c) => cloudSize = c.toInt
    //   case Array("--length-at-least", l) => minLength = l.toInt
    //   case Array("--window-size", w) => windowSize = w.toInt
    //   case _ => // Handle unexpected arguments if necessary
    // }

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
      val sortedWords = wordFrequency.toSeq.sortBy(-_._2).take(cloudSize)
      val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
      println(wordCloud)
    }

    // try {
    //   // Continuously read input
    //   Iterator.continually(StdIn.readLine()).takeWhile(_.nonEmpty).foreach { line =>

        // val words = line.split("\\s+").filter(_.length >= minLength)
      val lines = scala.io.Source.stdin.getLines()
      val words = 
        import scala.language.unsafeNulls
        lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+")).filter(_.length >= minLength)
      words.foreach { word =>
        updateWindow(word)
        if (window.size >= windowSize) {
          printWordCloud()
        }
      }
    //   }
    // } catch {
    //   case _: java.io.IOException => sys.exit(0) // Gracefully handle SIGPIPE
    // }

end Main



/* 

Additional Code for the Extra Credit

var cloudSize = 10
    var minLength = 6
    var windowSize = 1000
    // var cloudSize = 3
    // var minLength = 2
    // var windowSize = 5
    var updateSteps = 1
    var minFrequency = 1
    var ignoreList: Set[String] = Set.empty

    // Parse command-line arguments
    // args.sliding(2, 2).foreach {
    //   case Array("--cloud-size", c) => cloudSize = c.toInt
    //   case Array("--length-at-least", l) => minLength = l.toInt
    //   case Array("--window-size", w) => windowSize = w.toInt
    //   case _ => // Ignore unexpected arguments
    // }
    // Refined version of the args that are send by the user.
    args.sliding(2, 2).foreach {
      case Array("--cloud-size", c) => cloudSize = c.toInt
      case Array("--length-at-least", l) => minLength = l.toInt
      case Array("--window-size", w) => windowSize = w.toInt
      case Array("--update-every", k) => updateSteps = k.toInt
      case Array("--min-frequency", f) => minFrequency = f.toInt
      case Array("--ignore-file", path) =>
        // Load the ignore list from the file
        ignoreList = scala.io.Source.fromFile(path).getLines().map(_.toLowerCase).toSet
      case _ => // Handle unexpected arguments if necessary
    }

    // println(s"Cloud size: $cloudSize, Min length: $minLength, Window size: $windowSize")
    println(s"Cloud size: $cloudSize, Min length: $minLength, Window size: $windowSize, Update every $updateSteps steps, Min frequency: $minFrequency")

    // Sliding window to track the last N words
    val window = mutable.Queue[String]()

    // Map to track word frequencies within the window
    val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)

    // Create a dynamic pie chart for the word cloud
    // val chart = new PieChartBuilder().width(800).height(600).title("Word Cloud").build()
    // val sw = new SwingWrapper(chart)
    // sw.displayChart()

    // def updateWindow(word: String): Unit = {
    //   window.enqueue(word)
    //   wordFrequency(word) += 1
    //   if (window.size > windowSize) {
    //     val oldWord = window.dequeue()
    //     wordFrequency(oldWord) -= 1
    //     if (wordFrequency(oldWord) == 0) wordFrequency.remove(oldWord)
    //   }
    // }

    // Function to update the sliding window and word frequency
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

    // def printWordCloud(): Unit = {
    //   val sortedWords = wordFrequency.toSeq.sortBy(-_._2).take(cloudSize)
    //   val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
    //   println(wordCloud)
    // }
    // Function to print the word cloud and update the chart
    def printWordCloud(): Unit = {
      // Sort words by frequency and apply filters (minFrequency and cloudSize)
      val sortedWords = wordFrequency.toSeq
        .filter(_._2 >= minFrequency) // Only include words with sufficient frequency
        .sortBy(-_._2)
        .take(cloudSize)

      val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
      println(wordCloud)

      // Update chart for dynamic visualization
      // updateChart(sortedWords)
    }

    // Function to update the dynamic chart visualization
    // def updateChart(sortedWords: Seq[(String, Int)]): Unit = {
    //   chart.getSeriesMap.clear() // Clear the previous chart
    //   sortedWords.foreach { case (word, freq) =>
    //     chart.addSeries(word, freq)
    //   }
    //   sw.repaintChart() // Refresh the chart window
    // }

    var steps = 0


    try {
      // Continuously read input
      Iterator.continually(StdIn.readLine()).takeWhile(_.nonEmpty).foreach { line =>
        // Split the input into words, filter based on length and ignore list, and convert to lowercase
        val words = line.split("\\s+")
          .map(_.toLowerCase) // Convert to lowercase for case-insensitivity
          .filter(word => word.length >= minLength && !ignoreList.contains(word))

        // Process each word
        words.foreach { word =>
          updateWindow(word)
          steps += 1

          // Update and print word cloud every `updateSteps`
          if (window.size >= windowSize && steps % updateSteps == 0) {
            printWordCloud()
          }
        }
      }
    } catch {
      case _: java.io.IOException => sys.exit(0) // Gracefully handle SIGPIPE
    }
    // try {
    //   Iterator.continually(StdIn.readLine()).takeWhile(_.nonEmpty).foreach { line =>
    //     val words = line.split("\\s+").filter(_.length >= minLength) // needs to be changed similar to the project 1a
    //     words.foreach { word =>
    //       updateWindow(word)
    //       if (window.size >= windowSize) {
    //         printWordCloud()
    //       }
    //     }
    //   }
    // } catch {
    //   case _: java.io.IOException => sys.exit(0) // Gracefully handle SIGPIPE
    // }
    // Process the words and update the word cloud
    // processWords(windowSize, minLength, cloudSize)

    // Second part of the code. 
    // Default values
    // var cloudSize = 10
    // var minLength = 6
    // var windowSize = 1000

    // // Parse command-line arguments
    // args.sliding(2, 2).foreach {
    //   case Array("--cloud-size", c) => cloudSize = c.toInt
    //   case Array("--length-at-least", l) => minLength = l.toInt
    //   case Array("--window-size", w) => windowSize = w.toInt
    //   case _ => // Handle unexpected arguments if necessary
    // }

    // println(s"Cloud size: $cloudSize, Min length: $minLength, Window size: $windowSize")


    // // Sliding window to track the last N words
    // val window = mutable.Queue[String]()

    // // Map to track word frequencies within the window
    // val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)

    // def updateWindow(word: String): Unit = {
    //   window.enqueue(word)
    //   wordFrequency(word) += 1

    //   // Remove the oldest word if the window exceeds size
    //   if (window.size > windowSize) {
    //     val oldWord = window.dequeue()
    //     wordFrequency(oldWord) -= 1
    //     if (wordFrequency(oldWord) == 0) {
    //       wordFrequency.remove(oldWord)
    //     }
    //   }
    // }

    // // Function to print the word cloud
    // def printWordCloud(): Unit = {
    //   val sortedWords = wordFrequency.toSeq.sortBy(-_._2).take(cloudSize)
    //   val wordCloud = sortedWords.map { case (word, freq) => s"$word: $freq" }.mkString(" ")
    //   println(wordCloud)
    // }

    // try {
    //   // Continuously read input
    //   Iterator.continually(StdIn.readLine()).takeWhile(_.nonEmpty).foreach { line =>
    //     val words = line.split("\\s+").filter(_.length >= minLength)
    //     words.foreach { word =>
    //       updateWindow(word)
    //       if (window.size >= windowSize) {
    //         printWordCloud()
    //       }
    //     }
    //   }
    // } catch {
    //   case _: java.io.IOException => sys.exit(0) // Gracefully handle SIGPIPE
    // }
  // Default values
  // var cloudSize = 10
  // var minLength = 6
  // var windowSize = 1000

  // // Parse command-line arguments
  // args.sliding(2, 2).foreach {
  //   case Array("--cloud-size", c) => cloudSize = c.toInt
  //   case Array("--length-at-least", l) => minLength = l.toInt
  //   case Array("--window-size", w) => windowSize = w.toInt
  //   case _ => // Handle any other case
  // }

  // // A mutable queue to maintain the window of the last N words
  // val wordQueue = mutable.Queue[String]()

  // // A map to maintain the frequency count of words
  // val wordCount = mutable.Map[String, Int]().withDefaultValue(0)

  // // Function to print the word cloud
  // def printWordCloud(): Unit = {
  //   val sortedWords = wordCount.toSeq.sortBy(-_._2).take(cloudSize)
  //   println(sortedWords.map { case (word, count) => s"$word: $count" }.mkString(" "))
  // }

  // // Read input continuously until EOF
  // Iterator.continually(StdIn.readLine()).takeWhile(_ != null).foreach { line =>
  //   val words = line.split("\\s+").filter(_.length >= minLength)
  //   words.foreach { word =>
  //     wordQueue.enqueue(word)
  //     wordCount(word) += 1

  //     // Maintain the window size
  //     if (wordQueue.size > windowSize) {
  //       val removedWord = wordQueue.dequeue()
  //       wordCount(removedWord) -= 1
  //       if (wordCount(removedWord) == 0) {
  //         wordCount.remove(removedWord)
  //       }
  //     }

  //     // Print the word cloud after processing each word
  //     printWordCloud()
  //   }
  // }

  // // Graceful exit when EOF is encountered (Ctrl+D)
  // println("Program terminated.")



 */
