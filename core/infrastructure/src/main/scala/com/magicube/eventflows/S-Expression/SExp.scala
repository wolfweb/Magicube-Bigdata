package com.magicube.eventflows.SExpression

import scala.collection.immutable.Stream

abstract class SExp {
  def toList: List[SExp]

  def isList: Boolean
}

object SExp {
  private def streamFromIterator[A](iterator: Iterator[A]): Stream[A] = {
    if (iterator.hasNext) {
      return iterator.next() #:: streamFromIterator(iterator)
    } else {
      return Stream.empty
    }
  }

  def from(input: String): SExp = {
    val parser = new SExpParser(streamFromIterator(input.toIterator))
    parser.nextSExp()
  }
}

case class SInt(val value: Int) extends SExp {
  override def toString = value.toString()

  def toList = throw InconvertibleException()

  val isList = false
}

case class SDouble(val value: Double) extends SExp {
  override def toString = value.toString()

  def toList = throw InconvertibleException()

  val isList = false
}

case class SSymbol(val value: String) extends SExp {
  override def toString = value

  def toList = throw InconvertibleException()

  val isList = false
}

case class STrue() extends SExp {
  override def toString = "#t"

  def toList = throw InconvertibleException()

  val isList = false
}

case class SFalse() extends SExp {
  override def toString = "#f"

  def toList = throw InconvertibleException()

  val isList = false
}

case class SCons(val car: SExp, val cdr: SExp) extends SExp {
  override def toString = "(" + toList.mkString(" ") + ")"

  def toList: List[SExp] = car :: cdr.toList

  val isList = cdr.isList
}

case class SNil() extends SExp {
  override def toString = "null"

  def toList = List.empty

  val isList = true
}

object SList {
  def unapplySeq(sexp: SExp): Option[List[SExp]] = {
    if (sexp.isList) {
      Some(sexp.toList)
    } else {
      None
    }
  }
}

private trait SExpToken

private case object LPAR extends SExpToken

private case object RPAR extends SExpToken

private case object EOS extends SExpToken

private case class INT(value: Int) extends SExpToken

private case class DOUBLE(value: Double) extends SExpToken

private case class HASH(value: String) extends SExpToken

private case class ID(value: String) extends SExpToken

class SExpParser(private val input: Stream[Char]) {
  private val lex = new SExpLexer(input)

  def nextFile(): List[SExp] =
    lex.peek() match {
      case EOS => List.empty
      case _ => {
        val head = nextSExp()
        val tail = nextFile()
        head :: tail
      }
    }

  def nextSExp(): SExp =
    lex.peek() match {
      case EOS => throw ParseException("expected s-exp; got end of input")
      case LPAR => {
        lex.eatLPAR()
        val sexp = nextSExpList()
        lex.eatRPAR()
        sexp
      }
      case INT(value) => {
        lex.next()
        SInt(value)
      }
      case DOUBLE(value) => {
        lex.next()
        SDouble(value)
      }
      case ID(value) => {
        lex.next()
        SSymbol(value)
      }
      case HASH("t") => {
        lex.next()
        STrue()
      }
      case HASH("f") => {
        lex.next()
        SFalse()
      }
    }

  private def nextSExpList(): SExp =
    lex.peek() match {
      case RPAR => SNil()
      case _ => {
        val head = nextSExp()
        val tail = nextSExpList()
        SCons(head, tail)
      }
    }
}

private class SExpLexer(private var input: Stream[Char]) {
  private var nextTokens: List[SExpToken] = List.empty

  private var nextTokensTail: List[SExpToken] = List.empty

  def emit(token: SExpToken) {
    nextTokensTail = token :: nextTokensTail
  }

  private var state: SExpLexerState = INWHITESPACE

  private trait SExpLexerState {
    def process(c: Char): SExpLexerState

    def processEOF(): SExpLexerState
  }

  private case object DONE extends SExpLexerState {
    def processEOF(): SExpLexerState = {
      emit(EOS)
      return DONE
    }

    def process(c: Char): SExpLexerState = {
      throw ImpossibleException()
    }
  }

  private case object INCOMMENT extends SExpLexerState {
    def processEOF(): SExpLexerState = {
      emit(EOS)
      return DONE
    }

    def process(c: Char): SExpLexerState = {
      if (c == '\n')
        return INWHITESPACE
      else
        return INCOMMENT
    }
  }

  private case class INID(buf: List[Char]) extends SExpLexerState {
    def processEOF(): SExpLexerState = {
      emit(ID(buf.reverse.mkString))
      return DONE
    }

    def process(c: Char): SExpLexerState = {
      if (c.isWhitespace) {
        emit(ID(buf.reverse.mkString))
        return INWHITESPACE
      }

      c match {
        case ';' => {
          emit(ID(buf.reverse.mkString))
          return INCOMMENT
        }
        case '(' => {
          emit(ID(buf.reverse.mkString))
          emit(LPAR)
          return INWHITESPACE
        }
        case ')' => {
          emit(ID(buf.reverse.mkString))
          emit(RPAR)
          return INWHITESPACE
        }
        case _ => {
          return INID(c :: buf)
        }
      }

      throw ImpossibleException()
    }
  }

  private case class INHASH(buf: List[Char]) extends SExpLexerState {
    def processEOF(): SExpLexerState = {
      emit(HASH(buf.reverse.mkString))
      return DONE
    }

    def process(c: Char): SExpLexerState = {
      if (c.isWhitespace) {
        emit(HASH(buf.reverse.mkString))
        return INWHITESPACE;
      }
      c match {
        case ';' => {
          emit(HASH(buf.reverse.mkString))
          return INCOMMENT
        }
        case '(' => {
          emit(HASH(buf.reverse.mkString))
          emit(LPAR)
          return INWHITESPACE
        }
        case ')' => {
          emit(HASH(buf.reverse.mkString))
          emit(RPAR)
          return INWHITESPACE
        }
        case _ => {
          return INHASH(c :: buf)
        }
      }
      throw ImpossibleException()
    }
  }

  private case class INNUM(buf: List[Char], isfloat: Boolean = false) extends SExpLexerState {
    def processEOF(): SExpLexerState = {
      emit(INT(buf.reverse.mkString.toInt))
      return DONE
    }

    def process(c: Char): SExpLexerState = {
      if (c.isDigit) {
        return INNUM(c :: buf, isfloat)
      } else if (c == '.') {
        return INNUM(c :: buf, true)
      }
      if (isfloat)
        emit(DOUBLE(buf.reverse.mkString.toDouble))
      else
        emit(INT(buf.reverse.mkString.toInt))

      val old = input
      input = c #:: old
      return INWHITESPACE
    }
  }

  private case object INWHITESPACE extends SExpLexerState {
    def processEOF(): SExpLexerState = {
      emit(EOS)
      return DONE
    }

    def process(c: Char): SExpLexerState = {
      if (c.isWhitespace)
        return INWHITESPACE

      if (c.isDigit) {
        return INNUM(List(c))
      }

      c match {
        case ';' => return INCOMMENT
        case '#' => return INHASH(List())
        case '(' => {
          emit(LPAR)
          return INWHITESPACE
        }
        case ')' => {
          emit(RPAR)
          return INWHITESPACE
        }

        case _ => return INID(List(c))
      }
    }
  }

  private def loadTokens() {
    if (!nextTokens.isEmpty)
      return

    if (!nextTokensTail.isEmpty) {
      nextTokens = nextTokensTail.reverse
      nextTokensTail = List.empty
      return
    }

    if (input.isEmpty) {
      state = state.processEOF()
      // This had better load a token:
      nextTokens = nextTokensTail.reverse
      nextTokensTail = List.empty
      return
    }

    while (nextTokensTail.isEmpty && !input.isEmpty) {
      val c = input.head
      input = input.tail
      state = state.process(c)
    }

    if (input.isEmpty)
      state = state.processEOF()

    nextTokens = nextTokensTail.reverse
    nextTokensTail = List.empty
  }

  def peek(): SExpToken = {
    loadTokens()
    return nextTokens.head
  }

  def next(): SExpToken = {
    loadTokens()
    val t = nextTokens.head
    nextTokens = nextTokens.tail
    return t
  }

  def eatLPAR() =
    next() match {
      case LPAR => {}
      case t => throw ParseException("expected: '('; got: " + t)
    }

  def eatRPAR() =
    next() match {
      case RPAR => {}
      case t => throw ParseException("expected: ')'; got: " + t)
    }
}
