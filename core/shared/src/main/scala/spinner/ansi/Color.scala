package spinner.ansi

sealed trait Color {
  def toAnsiCode: String
  def toAnsiBrightCode: String
}

object Color {
  final case object Black extends Color {
    override def toAnsiCode: String = Console.BLACK
    override def toAnsiBrightCode: String = Console.BLACK_B
  }
  final case object Red extends Color {
    override def toAnsiCode: String = Console.RED
    override def toAnsiBrightCode: String = Console.RED_B
  }
  final case object Green extends Color {
    override def toAnsiCode: String = Console.GREEN
    override def toAnsiBrightCode: String = Console.GREEN_B
  }
  final case object Yellow extends Color {
    override def toAnsiCode: String = Console.YELLOW
    override def toAnsiBrightCode: String = Console.YELLOW_B
  }
  final case object Blue extends Color {
    override def toAnsiCode: String = Console.BLUE
    override def toAnsiBrightCode: String = Console.BLUE_B
  }
  final case object Magenta extends Color {
    override def toAnsiCode: String = Console.MAGENTA
    override def toAnsiBrightCode: String = Console.MAGENTA_B
  }
  final case object Cyan extends Color {
    override def toAnsiCode: String = Console.CYAN
    override def toAnsiBrightCode: String = Console.CYAN_B
  }
  final case object White extends Color {
    override def toAnsiCode: String = Console.WHITE
    override def toAnsiBrightCode: String = Console.WHITE_B
  }
}