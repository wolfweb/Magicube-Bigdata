package com.magicube.eventflows.SExpression

abstract class Exp {}

object Exp {
  def from(sexp: SExp): Exp = {
    sexp match {
      case SSymbol(id) => RefExp(id)
      case SList(SSymbol("lambda"), params, body) => {
        val vars = params.toList map { case SSymbol(id) => id }
        LambdaExp(vars, from(body))
      }
      case STrue() => BoolExp(true)
      case SFalse() => BoolExp(false)
      case SInt(value) => IntExp(value)
      case SDouble(value) => DoubleExp(value)
      case SList(SSymbol("if"), cond, ifTrue, ifFalse) => IfExp(from(cond), from(ifTrue), from(ifFalse))
      case SList(SSymbol("and"), a, b) => AndExp(from(a), from(b))
      case SList(SSymbol("or"), a, b) => OrExp(from(a), from(b))
      case SList(SSymbol("zero?"), arg) => ZeroPExp(from(arg))
      case SList(SSymbol("-"), a, b) => SubExp(from(a), from(b))
      case SList(SSymbol("+"), a, b) => PlusExp(from(a), from(b))
      case SList(SSymbol("*"), a, b) => TimesExp(from(a), from(b))
      case SList(SSymbol("="), a, b) => EqExp(from(a), from(b))
      case SList(SSymbol(">"), a, b) => GreaterThan(from(a), from(b))
      case SList(SSymbol(">="), a, b) => GreaterEqThan(from(a), from(b))
      case SList(SSymbol("<"), a, b) => LessThan(from(a), from(b))
      case SList(SSymbol("<="), a, b) => LessEqThan(from(a), from(b))
      case SList(SSymbol("contains"), a, b) => ContainsExp(from(a), from(b))
      case SList(SSymbol("cons"), car, cdr) => ConsExp(from(car), from(cdr))
      case SList(SSymbol("car"), arg) => CarExp(from(arg))
      case SList(SSymbol("cdr"), arg) => CdrExp(from(arg))
      case SList(SSymbol("quote"), SList()) => NullExp()
      case SList(SSymbol("pair?"), arg) => PairPExp(from(arg))
      case SList(SSymbol("null?"), arg) => NullPExp(from(arg))
      case SList(SSymbol("let"), bindings, body) => {
        val varexps = bindings.toList map {
          case SList(SSymbol(id), exp) => (id, from(exp))
        }
        val (vars, exps) = varexps.unzip
        LetExp(vars, exps, from(body))
      }
      case SList(SSymbol("letrec"), SList(SList(SSymbol(fun), lambda)), body) => LetRecExp(fun, from(lambda), from(body))
      case SCons(fun, args) => AppExp(from(fun), args.toList map from)
    }
  }
}

case class RefExp(val id: String) extends Exp {
  override def toString = id
}

case class LambdaExp(val params: List[String], val body: Exp) extends Exp {
  override def toString = "(lambda (" + params.mkString(" ") + ") " + body + ")"
}

case class AppExp(val fun: Exp, args: List[Exp]) extends Exp {
  override def toString = "(" + (fun :: args).mkString(" ") + ")"
}

case class BoolExp(val value: Boolean) extends Exp {
  override def toString: String = value.toString
}

case class IntExp(val value: Int) extends Exp {
  override def toString: String = value.toString
}

case class DoubleExp(val value: Double) extends Exp {
  override def toString: String = value.toString
}

case class IfExp(val cond: Exp, val ifTrue: Exp, val ifFalse: Exp) extends Exp

case class AndExp(val cond1: Exp, val cond2: Exp) extends Exp

case class OrExp(val cond1: Exp, val cond2: Exp) extends Exp

case class ZeroPExp(val test: Exp) extends Exp

case class SubExp(val exp1: Exp, val exp2: Exp) extends Exp

case class EqExp(val exp1: Exp, val exp2: Exp) extends Exp

case class GreaterThan(val exp1: Exp, val exp2: Exp) extends Exp

case class GreaterEqThan(val exp1: Exp, val exp2: Exp) extends Exp

case class LessThan(val exp1: Exp, val exp2: Exp) extends Exp

case class LessEqThan(val exp1: Exp, val exp2: Exp) extends Exp

case class PlusExp(val exp1: Exp, val exp2: Exp) extends Exp

case class TimesExp(val exp1: Exp, val exp2: Exp) extends Exp

case class LetExp(val vars: List[String], val exps: List[Exp], val body: Exp) extends Exp

case class LetRecExp(val fun: String, val lambda: Exp, val body: Exp) extends Exp

case class ContainsExp(val car: Exp, val cdr: Exp) extends Exp

case class ConsExp(val car: Exp, val cdr: Exp) extends Exp

case class CarExp(val arg: Exp) extends Exp

case class CdrExp(val arg: Exp) extends Exp

case class PairPExp(val arg: Exp) extends Exp

case class NullPExp(val arg: Exp) extends Exp

case class NullExp() extends Exp
